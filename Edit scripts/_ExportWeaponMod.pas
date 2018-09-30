unit _ExportWeaponMod;

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
    outputLines.Add('  "formID": '    + IntToStr(FormID(e))                                                  +  ',');
    outputLines.Add('  "editorID": "' + GetEditValue(ElementBySignature(e, 'EDID'))                          + '",');
    outputLines.Add('  "name": "'     + GetEditValue(ElementBySignature(e, 'FULL'))                          + '",');
    outputLines.Add('  "value": '     + GetEditValue(ElementByName(ElementBySignature(e, 'DATA'), 'Value'))  +  ',');
    outputLines.Add('  "weight": '    + GetEditValue(ElementByName(ElementBySignature(e, 'DATA'), 'Weight')) +  ',');
    outputLines.Add('},');
end;

function Finalize: integer;
begin
    outputLines.Add(']');

    if (outputLines.Count > 0) then
    begin
        CreateDir('fallout-weaponmods/');
        outputLines.SaveToFile('fallout-weaponmods/misc.json');
    end;
end;


end.
