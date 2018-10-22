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
    data: IwbElement;

    i: integer;

    keywords: IwbContainer;
    keyword: IwbElement;
begin
    if (CompareText(Signature(e), 'WEAP') <> 0) then
    begin
        exit;
    end;


    // Init
    outputLines.Add('{');


    // General
    data := ElementBySignature(e, 'DNAM');
    outputLines.Add('  "file": "'       + GetFileName(GetFile(e))                            + '",');
    outputLines.Add('  "formID": "'     + IntToHex(FormID(e), 8)                             + '",');
    outputLines.Add('  "editorID": "'   + GetEditValue(ElementBySignature(e, 'EDID'))        + '",');
    outputLines.Add('  "name": "'       + GetEditValue(ElementBySignature(e, 'FULL'))        + '",');
    outputLines.Add('  "speed": '       + GetEditValue(ElementByPath(data, 'Speed'))         +  ',');
    outputLines.Add('  "reloadSpeed": ' + GetEditValue(ElementByPath(data, 'Reload Speed'))  +  ',');
    outputLines.Add('  "reach": '       + GetEditValue(ElementByPath(data, 'Reach'))         +  ',');
    outputLines.Add('  "minRange": '    + GetEditValue(ElementByPath(data, 'Min Range'))     +  ',');
    outputLines.Add('  "maxRange": '    + GetEditValue(ElementByPath(data, 'Max Range'))     +  ',');
    outputLines.Add('  "attackDelay": ' + GetEditValue(ElementByPath(data, 'Attack Delay'))  +  ',');
    outputLines.Add('  "weight": '      + GetEditValue(ElementByPath(data, 'Weight'))        +  ',');
    outputLines.Add('  "value": '       + GetEditValue(ElementByPath(data, 'Value'))         +  ',');
    outputLines.Add('  "baseDamage": '  + GetEditValue(ElementByPath(data, 'Damage - Base')) +  ',');


    // Keywords
    outputLines.Add('  "keywords": [');

    keywords := ElementBySignature(e, 'KWDA');
    for i := 0 to ElementCount(keywords) - 1 do
    begin
        keyword := ElementByIndex(keywords, i);

        outputLines.Add('    "' + NameToEditorID(GetEditValue(keyword)) + '",');
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
        outputLines.SaveToFile('fallout-weaponmods/weap.json');
    end;
end;


end.
