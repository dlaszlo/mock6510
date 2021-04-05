@echo off
set curr_dir=%~dp0
docker run --rm -v %curr_dir%:/source dlaszlo/64tass make %*
