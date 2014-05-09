# Builds the project completely

fail() {
	echo "Build failed! Aborting."
	exit 2
}

cd ../kraftrpg-api
#mvn clean install || fail
cd ../kraftrpg-skills
#mvn clean install || fail
cd ../kraftrpg
mvn clean install || fail

echo
echo "Building .zip file for release."

rm -rf build/
mkdir build

cp ../kraftrpg-skills/target/KraftRPGBundledSkills.jar build/
cp target/KraftRPG.jar build/

cd build
zip -v KraftRPG.zip *.jar || fail

echo
echo "Build success! The file is at build/KraftRPG.zip"
echo
exit 0

