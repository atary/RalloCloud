set vmRAM=%1
set vmBW=%2
set vmNUM=%3
MKDIR dist\out
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar AFF %vmRAM% %vmBW% %vmNUM% %%G/30
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar ANF %vmRAM% %vmBW% %vmNUM% %%G/30
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar LBG %vmRAM% %vmBW% %vmNUM% %%G/30
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar LFF %vmRAM% %vmBW% %vmNUM% %%G/30
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar RAN %vmRAM% %vmBW% %vmNUM% %%G/30
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar TBF %vmRAM% %vmBW% %vmNUM% %%G/30
FOR /L %%G IN (1,1,30) DO START /B /WAIT java -jar dist/RalloCloud.jar LNF %vmRAM% %vmBW% %vmNUM% %%G/30