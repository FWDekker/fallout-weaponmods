unit _ExportMISC;

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
    if (CompareText(Signature(e), 'MISC') <> 0) then
    begin
        exit;
    end;

    if (not ListContains(ElementBySignature(e, 'KWDA'), 'ObjectTypeLooseMod')) then
    begin
        exit;
    end;

    outputLines.Add('{');
    outputLines.Add('  "formID": '    + IntToStr(FormID(e))                                                                   +  ',');
    outputLines.Add('  "editorID": "' + GetEditValue(ElementBySignature(e, 'EDID'))                                           + '",');
    outputLines.Add('  "name": "'     + EscapeJsonString(GetEditValue(ElementBySignature(e, 'FULL')))                         + '",');
    outputLines.Add('  "model": "'    + EscapeJsonString(GetEditValue(ElementBySignature(ElementByPath(e, 'Model'), 'MODL'))) + '",');
    outputLines.Add('  "value": '     + GetEditValue(ElementByName(ElementBySignature(e, 'DATA'), 'Value'))                   +  ',');
    outputLines.Add('  "weight": '    + GetEditValue(ElementByName(ElementBySignature(e, 'DATA'), 'Weight'))                  +  ',');
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
