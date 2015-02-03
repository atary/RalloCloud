REM RD /S /Q dist\out
set vmRAM=9
MKDIR dist\out
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar AFF %vmRAM%
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar ANF %vmRAM%
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar LBG %vmRAM%
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar LFF %vmRAM%
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar RAN %vmRAM%
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar TBF %vmRAM%