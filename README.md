# WECollect - Windows Event Collect

Check out the new innovative forensic tools that can retrieve events from Windows post-mortem images.

This is a client-server architecture, and this is a client, 
you need to have the server running on your machine or remotely, see +info here.

## Requirements:

- Windows 10 disk image mounted in the system
(You need to have a raw image of your disk e.g: convert .VDI to .IMG `VBoxManage clonehd <nameOfOriginalVDI>.vdi <nameOfNewImage>.img --format RAW`)
- For MacOS/Linux users: you need to be able to read `NTFS` 
- Java 8
- Python 2.7
- `python-evtx` - `pip install python-evtx`

## Usage:

```
java wecollect -windir <windows directory> -h <hostname> -u <username> -p <password>
```

### Arguments:

To execute the program you need to pass this arguments:


`-windir` : the path to the `Windows' directory of the disk image (e.g: /Volumes/DiskImg/Windows)

To access the database your need to specify:

`-h` : hostname

`-u` : username

`-p` : password
