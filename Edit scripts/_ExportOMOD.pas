unit _ExportOMOD;

uses
    WeaponModCore;

var
    outputLines: TStringList;


function Initialize: integer;
begin
    outputLines := TStringList.Create;
    outputLines.Add('[');
end;

function Process(e: IInterface): integer;
var
    i: integer;

    effects: IwbContainer;
    effect: IwbElement;
begin
    if (CompareText(Signature(e), 'OMOD') <> 0) then
    begin
        exit;
    end;

    if (ListContains(ElementBySignature(e, 'MNAM'), 'ma_TEMPLATE')) then
    begin
        exit;
    end;

    if (CompareText(GetEditValue(ElementByPath(ElementBySignature(e, 'DATA'), 'Form Type')), 'Weapon') <> 0) then
    begin
        exit;
    end;



    // Init
    outputLines.Add('{');


    // General
    outputLines.Add('  "file": "'        + GetFileName(GetFile(e))                                                                          + '",');
    outputLines.Add('  "formID": "'      + IntToHex(FormID(e), 8)                                                                           + '",');
    outputLines.Add('  "editorID": "'    + GetEditValue(ElementBySignature(e, 'EDID'))                                                      + '",');
    outputLines.Add('  "name": "'        + GetEditValue(ElementBySignature(e, 'FULL'))                                                      + '",');
    outputLines.Add('  "description": "' + EscapeJsonString(GetEditValue(ElementBySignature(e, 'DESC')))                                    + '",');
    outputLines.Add('  "looseMod": "'    + NameToEditorID(GetEditValue(ElementBySignature(e, 'LNAM')))                                      + '",');
    outputLines.Add('  "weapon":     "'  + EscapeJsonString(NameToEditorID(GetEditValue(ElementByIndex(ElementBySignature(e, 'MNAM'), 0)))) + '",');


    // Effects
    outputLines.Add('  "effects": [');

    effects := ElementByPath(ElementBySignature(e, 'DATA'), 'Properties');
    for i := 0 to ElementCount(effects) - 1 do
    begin
        effect := ElementByIndex(effects, i);

        outputLines.Add('  {');
        outputLines.Add('    "valueType": "'      + GetEditValue(ElementByPath(effect, 'Value Type'))    + '",');
        outputLines.Add('    "functionType": "'   + GetEditValue(ElementByPath(effect, 'Function Type')) + '",');
        outputLines.Add('    "property": "'       + GetEditValue(ElementByPath(effect, 'Property'))      + '",');
        outputLines.Add('    "value1": '          + GetEffectValue1(effect)                              +  ',');
        outputLines.Add('    "value2": '          + GetEffectValue1(effect)                              +  ',');
        outputLines.Add('    "step": '            + GetEditValue(ElementByPath(effect, 'Step'))          +  ',');
        outputLines.Add('  },');
    end;

    outputLines.Add('  ],');


    // Finalise
    outputLines.Add('},');
end;

function Finalize: integer;
begin
    outputLines.Add(']');

    if (outputLines.Count > 0) then
    begin
        CreateDir('fallout-weaponmods/');
        outputLines.SaveToFile('fallout-weaponmods/omod.json');
    end;
end;


function GetEffectValue1(effect: IwbElement): string;
var
    i: integer;
    valueType: string;
begin
    valueType := GetEditValue(ElementByPath(effect, 'Value Type'));
    i := pos(',', valueType);
    valueType := copy(valueType, 1, i - 1);
    if (Length(valueType) = 0) then
    begin
        valueType := GetEditValue(ElementByPath(effect, 'Value Type'));
    end;

    Result := GetEditValue(ElementByPath(effect, 'Value 1 - ' + valueType));
    Result := FormatEffectValue(Result, valueType);
end;

function GetEffectValue2(effect: IwbElement): string;
var
    i: integer;
    valueType: string;
begin
    valueType := GetEditValue(ElementByPath(effect, 'Value Type'));
    i := pos(',', valueType);
    valueType := copy(valueType, i + 1, Length(valueType) - i);
    if (Length(valueType) = 0) then
    begin
        valueType := GetEditValue(ElementByPath(effect, 'Value Type'));
    end;

    Result := GetEditValue(ElementByPath(effect, 'Value 2 - ' + valueType));
    Result := FormatEffectValue(Result, valueType);
end;

function FormatEffectValue(effectValue: string; valueType: string): string;
begin
    Result := effectValue;

    if (CompareText(valueType, 'FormID') = 0) then
    begin
        Result := NameToEditorID(Result);
    end;

    if ((CompareText(valueType, 'Int') <> 0) AND (CompareText(valueType, 'Float') <> 0)) then
    begin
        Result := '"' + Result + '"';
    end;
end;


end.
