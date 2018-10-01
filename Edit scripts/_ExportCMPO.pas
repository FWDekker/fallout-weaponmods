unit _ExportCMPO;

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
begin
    if (CompareText(Signature(e), 'CMPO') <> 0) then
    begin
        exit;
    end;

    outputLines.Add('{');
    outputLines.Add('  "formID": '    + IntToStr(FormID(e))                         +  ',');
    outputLines.Add('  "editorID": "' + GetEditValue(ElementBySignature(e, 'EDID')) + '",');
    outputLines.Add('  "name": "'     + GetEditValue(ElementBySignature(e, 'FULL')) + '",');
    outputLines.Add('}');
end;

function Finalize: integer;
begin
    outputLines.Add(']');

    if (outputLines.Count > 0) then
    begin
        CreateDir('fallout-weaponmods/');
        outputLines.SaveToFile('fallout-weaponmods/cmpo.json');
    end;
end;


end.
