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
begin
    if (CompareText(Signature(e), 'OMOD') <> 0) then
    begin
        exit;
    end;

    outputLines.Add('{');
    outputLines.Add('  "formID": '       + IntToStr(FormID(e))                                                                              +  ',');
    outputLines.Add('  "editorID": "'    + GetEditValue(ElementBySignature(e, 'EDID'))                                                      + '",');
    outputLines.Add('  "name": "'        + GetEditValue(ElementBySignature(e, 'FULL'))                                                      + '",');
    outputLines.Add('  "description": "' + EscapeJsonString(GetEditValue(ElementBySignature(e, 'DESC')))                                    + '",');
    outputLines.Add('  "looseMod": "'    + NameToEditorID(GetEditValue(ElementBySignature(e, 'LNAM')))                                      + '",');
    outputLines.Add('  "weaponName": "'  + EscapeJsonString(NameToEditorID(GetEditValue(ElementByIndex(ElementBySignature(e, 'MNAM'), 0)))) + '",');
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


end.
