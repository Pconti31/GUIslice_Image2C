; GUIsliceBuilder Inno Setup SKELETON Script
;
; PLEASE NOTE:
;
; 1. This script is a SKELETON and is meant to be parsed by the Gradle 
;    task "innosetup" before handing it to the Inno Setup compiler (ISCC)
;
; 2. All VARIABLES with a dollar sign and curly brackets are replaced
;    by Gradle, e.g. "applicationVersion" below
;
; 3. The script is COPIED to build/innosetup before its run,
;    so all relative paths refer to this path!
;
; 4. All BACKSLASHES must be escaped 
;

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
ArchitecturesInstallIn64BitMode=x64 ia64
AppId={{B4E40094-A49F-434E-AA4F-BC25738BBCB7}
AppName=GUIslice_Image2C
AppVersion=${applicationVersion}
AppVerName=GUIslice_Image2C ${applicationVersion}
AppSupportURL=https://github.com/Pconti31/GUIslice_Image2C/issues
AppUpdatesURL=https://github.com/Pconti31/GUIslice_Image2C/releases
DefaultGroupName=GUIslice_Image2C
DefaultDirName={userdocs}\\GUIslice_Image2C
DisableDirPage=no
DisableWelcomePage=no
DisableProgramGroupPage=yes
LicenseFile=..\\..\\docs\\LICENSE.txt
OutputDir=.
OutputBaseFilename=image2c-win-${applicationVersion}
SetupIconFile=..\\tmp\\windows\\GUIslice_Image2C\\GUIslice_Image2C.ico
Compression=lzma
SolidCompression=yes
PrivilegesRequired=none

[Setup]
; Tell Windows Explorer to reload the environment
ChangesEnvironment=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Dirs]
Name: "{app}"; 
Name: "{app}\\logs"; Permissions: everyone-full

[Files]
Source: "..\\tmp\\windows\\GUIslice_Image2C\\image2c.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\\tmp\\windows\\GUIslice_Image2C\\GUIslice_Image2C.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\\tmp\\windows\\GUIslice_Image2C\\release"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\\tmp\\windows\\GUIslice_Image2C\\bin\\*"; DestDir: "{app}\\bin"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\\tmp\\windows\\GUIslice_Image2C\\conf\\*"; DestDir: "{app}\\conf"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\\tmp\\windows\\GUIslice_Image2C\\legal\\*"; DestDir: "{app}\\legal"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\\tmp\\windows\\GUIslice_Image2C\\lib\\*"; DestDir: "{app}\\lib"; Flags: ignoreversion recursesubdirs createallsubdirs
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{group}\\GUIslice_Image2C"; Filename: "{app}\\image2c.bat"
Name: "{commondesktop}\\GUIslice Image2C"; Filename: "{app}\\image2c.bat"; IconFilename: "{app}\\GUIslice_Image2C.ico"; Tasks: desktopicon

[Run]
Filename: "{app}\\image2c.bat"; Description: "{cm:LaunchProgram,GUIslice_Image2C}"; Flags: shellexec postinstall skipifsilent

[Code]

function IsRegularUser(): Boolean;
begin
Result := not (IsAdminLoggedOn or IsPowerUserLoggedOn);
end;

