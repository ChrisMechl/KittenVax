#1/bin/bash
fuser -k 3000/tcp &
json-server /home/chris/workspace/KittenVax/db.json
