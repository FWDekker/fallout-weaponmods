unit _ExportOMOD;

var
    outputLines: TStringList;


function Initialize: integer;
begin
    outputLines := TStringList.Create;
    outputLines.Add('[');
end;

function Process(e: IInterface): integer;
begin
    outputLines.Add('{');
    outputLines.Add('  "formID": '       + IntToStr(FormID(e))                                                            +  ',');
    outputLines.Add('  "editorID": "'    + GetEditValue(ElementBySignature(e, 'EDID'))                                    + '",');
    outputLines.Add('  "name": "'        + GetEditValue(ElementBySignature(e, 'FULL'))                                    + '",');
    outputLines.Add('  "description": "' + GetEditValue(ElementBySignature(e, 'DESC'))                                    + '",');
    outputLines.Add('  "looseMod": "'    + NameToEditorID(GetEditValue(ElementBySignature(e, 'LNAM')))                    + '",');
    outputLines.Add('  "weaponName": "'  + NameToEditorID(GetEditValue(ElementByIndex(ElementBySignature(e, 'MNAM'), 0))) + '",');
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


function NameToEditorID(s: string): string;
var
    index: integer;
begin
    index := pos(' ', s);
    Result := copy(s, 1, index - 1);
end;


end.
