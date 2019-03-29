@echo off
set cls=%1
echo ^>Starting %cls% client...
rem throw the first parameter away
shift
set params=%1
:loop
shift
if [%1]==[] goto afterloop
set params=%params% %1
goto loop
:afterloop
java -cp "target\classes\;target\dependency\*" test.%cls% %params%
