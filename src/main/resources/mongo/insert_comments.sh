#!/bin/bash

# Configuration variables
DB_NAME="blogdb_dev"
COLLECTION_NAME="comments"
FILE_PATH="comment_data.json"

mongoimport --db "$DB_NAME" --collection "$COLLECTION_NAME" --file "$FILE_PATH" --jsonArray --drop

if [ $? -eq 0 ]; then
    echo "Successfully imported data into $DB_NAME.$COLLECTION_NAME"
else
    echo "Error: Failed to import data."
    exit 1
fi
