# WECollect - Windows Event Collect
Check out the new innovative forensic tools that can retrieve events from Windows post-mortem images.

## Requirements:

- Windows 10 disk image mounted in the system
(You need to have a raw image of your disk e.g: .VDI to .IMG `VBoxManage clonehd <nameOfOriginalVDI>.vdi <nameOfNewImage>.img --format RAW`)
- For MacOS users: you need to be able to read `NTFS`
- Java SDK 8
- Maven 3.3
- Python 2.7
- `python-evtx` - `pip install python-evtx`

## Running:

This is a client-server architecture, and this is a client, 
you need to have the server running on your machine or remotely, see +info here.

To execute the program you need to pass the path to the `Windows`
 directory of the disk image:

`mvn package exec:java -Dexec.args="-windir ${WINDOWS_DIR_PATH}"`

To execute the program with custom database parameters:

`mvn package exec:java -Dexec.args="-windir ${WINDOWS_DIR_PATH} [-h hostname] [-u username] [-p password]"`

If you don't specify the password, the program will ask you to insert later on, without show on the screen what are you typing.

Example:

`WINDOWS_DIR_PATH="/Volumes/DiskImg/Windows"`

