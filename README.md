# WECollect
Check out the new innovative forensic tools that can retrieve events from Windows post-mortem images.

## Requirements:

- Java SDK 8
- Maven
- Python 2
- `python-evtx` - `pip install python-evtx`

## Running:

This is a client-server architecture, and this is a client, 
you need to have the server running on your machine or remotely, see +info here.

To execute the program you need to specify the directory of the disk image:

`mvn package exec:java -Dexec.args=${IMG_DIR}`

Example:

`IMG_DIR="/Users/username/Downloads/tree"`

