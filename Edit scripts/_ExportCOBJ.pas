unit _ExportCOBJ;

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

    components: IwbContainer;
    component: IwbElement;

    conditions: IwbContainer;
    condition: IwbElement;
    perk: string;
begin
    if (CompareText(Signature(e), 'COBJ') <> 0) then
    begin
        exit;
    end;

    if (CompareText(NameToSignature(GetEditValue(ElementBySignature(e, 'CNAM'))), 'OMOD') <> 0) then
    begin
        exit;
    end;


    // Init
    outputLines.Add('{');


    // General
    outputLines.Add('  "file": "'       + GetFileName(GetFile(e))                                                       + '",');
    outputLines.Add('  "formID": "'     + IntToHex(FormID(e), 8)                                                        + '",');
    outputLines.Add('  "editorID": "'   + GetEditValue(ElementBySignature(e, 'EDID'))                                   + '",');
    outputLines.Add('  "createdMod": "' + EscapeJsonString(NameToEditorID(GetEditValue(ElementBySignature(e, 'CNAM')))) + '",');


    // Components
    outputLines.Add('  "components": [');

    components := ElementBySignature(e, 'FVPA');
    for i := 0 to ElementCount(components) - 1 do
    begin
        component := ElementByIndex(components, i);

        outputLines.Add('  {');
        outputLines.Add('    "component": "' + NameToEditorID(GetEditValue(ElementByName(component, 'Component'))) + '",');
        outputLines.Add('    "count":      ' + GetEditValue(ElementByName(component, 'Count'))                     +  ',');
        outputLines.Add('  },');
    end;

    outputLines.Add('  ],');


    // Conditions
    outputLines.Add('  "conditions": [');

    conditions := ElementByName(e, 'Conditions');
    for i := 0 to ElementCount(conditions) - 1 do
    begin
        condition := ElementByIndex(conditions, i);
        condition := ElementBySignature(condition, 'CTDA');

        if (Length(GetEditValue(ElementByName(condition, 'Perk'))) = 0) then
        begin
            continue;
        end;

        perk := GetEditValue(ElementByName(condition, 'Perk'));
        outputLines.Add('  {');
        outputLines.Add('    "perk": "' + PerkName(NameToEditorID(perk)) + '",');
        outputLines.Add('    "rank": '  + PerkRank(NameToEditorID(perk)) +  ',');
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
        outputLines.SaveToFile('fallout-weaponmods/cobj.json');
    end;
end;


function PerkName(perk: string): string;
var
    i: integer;
begin
    i := pos('0', perk);
    Result := copy(perk, 1, i - 1);
end;

function PerkRank(perk: string): string;
var
    i: integer;
begin
    i := pos('0', perk);
    Result := copy(perk, i + 1, Length(perk) - i);
end;


end.
