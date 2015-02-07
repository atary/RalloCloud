REM RD /S /Q dist\out
set vmRAM=2
set vmBW=2
MKDIR dist\out
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar AFF %vmRAM% %vmBW%
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar ANF %vmRAM% %vmBW%
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar LBG %vmRAM% %vmBW%
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar LFF %vmRAM% %vmBW%
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar RAN %vmRAM% %vmBW%
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar TBF %vmRAM% %vmBW%