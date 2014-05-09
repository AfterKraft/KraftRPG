#!/bin/bash

# This script will make sure you have a build environment ready for KraftRPG development.
# It assumes that you want all 3 project directories in the same folder - namely, the folder they are currently in.

if [ ! -x setup.sh ]
then
	echo "You should run this script from the KraftRPG repository."
	exit 1
fi

cd ..

if [ ! -d kraftrpg ]
then
	echo "Folder structure doesn't matche expected. Is the capitalization different? Expecting this to be in a folder named 'kraftrpg'."
	exit 1
fi

git > /dev/null

if [ $? -eq 127 ]
then
	echo "It seems 'git' isn't installed. You should install 'git' before continuing."
	exit 1
fi

# git clone

if [ ! -d kraftrpg-api ]
then
	git clone http://git.afterkraft.com/afterkraft/kraftrpg-api.git || exit
fi
if [ ! -d kraftrpg-skills ]
then
	git clone http://git.afterkraft.com/afterkraft/kraftrpg-skills.git || exit
fi

# build
cd kraftrpg
./build.sh || exit

echo "Done!"

