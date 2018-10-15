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
    valueType1: string;
    valueType2: string;
    quote1: string;
    quote2: string;
begin
    if (CompareText(Signature(e), 'OMOD') <> 0) then
    begin
        exit;
    end;

    if (ListContains(ElementBySignature(e, 'MNAM'), 'ma_TEMPLATE')) then
    begin
        exit;
    end;


    // Init
    outputLines.Add('{');


    // General
    outputLines.Add('  "file": "'        + GetFileName(GetFile(e))                                                                          + '",');
    outputLines.Add('  "formID": '       + IntToStr(FormID(e))                                                                              +  ',');
    outputLines.Add('  "editorID": "'    + GetEditValue(ElementBySignature(e, 'EDID'))                                                      + '",');
    outputLines.Add('  "name": "'        + GetEditValue(ElementBySignature(e, 'FULL'))                                                      + '",');
    outputLines.Add('  "description": "' + EscapeJsonString(GetEditValue(ElementBySignature(e, 'DESC')))                                    + '",');
    outputLines.Add('  "looseMod": "'    + NameToEditorID(GetEditValue(ElementBySignature(e, 'LNAM')))                                      + '",');
    outputLines.Add('  "weaponName": "'  + EscapeJsonString(NameToEditorID(GetEditValue(ElementByIndex(ElementBySignature(e, 'MNAM'), 0)))) + '",');


    // Effects
    outputLines.Add('  "effects": [');

    effects := ElementByPath(ElementBySignature(e, 'DATA'), 'Properties');
    for i := 0 to ElementCount(effects) - 1 do
    begin
        effect := ElementByIndex(effects, i);

        valueType1 := BeforeComma(GetEditValue(ElementByPath(effect, 'Value Type')));
        valueType2 := AfterComma(GetEditValue(ElementByPath(effect, 'Value Type')));
        if (Length(valueType1) = 0) then
        begin
            valueType1 := valueType2;
        end;

        quote1 := '"';
        if ((CompareText(valueType1, 'Int') = 0) OR (CompareText(valueType1, 'Float') = 0)) then
        begin
            quote1 := '';
        end;

        quote2 := '"';
        if ((CompareText(valueType2, 'Int') = 0) OR (CompareText(valueType2, 'Float') = 0)) then
        begin
            quote2 := '';
        end;

        outputLines.Add('  {');
        outputLines.Add('    "valueType": "'      + GetEditValue(ElementByPath(effect, 'Value Type'))                       + '",');
        outputLines.Add('    "functionType": "'   + GetEditValue(ElementByPath(effect, 'Function Type'))                    + '",');
        outputLines.Add('    "property": "'       + GetEditValue(ElementByPath(effect, 'Property'))                         + '",');
        outputLines.Add('    "value1": ' + quote1 + GetEditValue(ElementByPath(effect, 'Value 1 - ' + valueType1)) + quote1 + ' ,');
        outputLines.Add('    "value2": ' + quote2 + GetEditValue(ElementByPath(effect, 'Value 2 - ' + valueType2)) + quote2 + ' ,');
        outputLines.Add('    "step": '            + GetEditValue(ElementByPath(effect, 'Step'))                             + ' ,');
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


function BeforeComma(s: string): string;
var
    i: integer;
begin
    i := pos(',', s);
    Result := copy(s, 1, i - 1);
end;

function AfterComma(s: string): string;
var
    i: integer;
begin
    i := pos(',', s);
    Result := copy(s, i + 1, Length(s) - i);
end;


end.
