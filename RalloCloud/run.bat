RD /S /Q dist\out
MKDIR dist\out
FOR /L %%G IN (1,1,30) DO START /WAIT java -jar dist/RalloCloud.jar AFF
FOR /L %%G IN (1,1,30) DO START /WAIT java -jar dist/RalloCloud.jar ANF
FOR /L %%G IN (1,1,30) DO START /WAIT java -jar dist/RalloCloud.jar LBG
FOR /L %%G IN (1,1,30) DO START /WAIT java -jar dist/RalloCloud.jar LFF
FOR /L %%G IN (1,1,30) DO START /WAIT java -jar dist/RalloCloud.jar RAN
FOR /L %%G IN (1,1,30) DO START /WAIT java -jar dist/RalloCloud.jar TBF