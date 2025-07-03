#!/bin/zsh

cd spotify-frontend

echo "Building frontend..."
npm run build

echo "Copying built files to static folder..."
cp -r dist/* /Users/ryanabdallahconrad/IdeaProjects/spotify-data-app/src/main/resources/static

echo "Restarting backend..."
cd ..
mvn spring-boot:run