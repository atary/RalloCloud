REM RD /S /Q dist\out
MKDIR dist\out
FOR /L %%G IN (1,1,8) DO START run.bat 4 4 %%G