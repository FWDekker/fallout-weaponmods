unit WeaponModCore;


function NameToEditorID(s: string): string;
var
    i: integer;
begin
    i := pos(' ', s);
    Result := copy(s, 1, i - 1);
end;

function NameToSignature(s: string): string;
var
	i: integer;
begin
	i := pos('[', s);
	Result := copy(s, i + 1, 4);
end;

function ListContains(c: IwbContainer; k: string): boolean;
var
	i: integer;
begin
	Result := false;

	for i := 0 to ElementCount(c) - 1 do
	begin
		if (CompareText(NameToEditorID(GetEditValue(ElementByIndex(c, i))), k) = 0) then
		begin
			Result := true;
		end;
	end;
end;

function EscapeJsonString(s: string): string;
begin
    Result := s;
    Result := StringReplace(Result, '\', '\\', (rfReplaceAll));
    Result := StringReplace(Result, '"', '\"', (rfReplaceAll));
    Result := StringReplace(Result, #13 + #10, '\n', (rfReplaceAll));
    Result := StringReplace(Result, #10, '\n', (rfReplaceAll));
end;


end.
