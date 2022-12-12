echo "Fetching assets..."
cp out/artifacts/Nerdle_jar/Nerdle.jar out/application/Nerdle.jar
cp nerdle.icns out/application
echo "Fetched assets"

cd out/application || exit
echo "Packaging JAR in /out/application..."
if jpackage --input "$PWD" --main-jar Nerdle.jar --icon nerdle.icns --name Nerdle; then
  echo "\u001b[32mBUILD SUCCESSFUL\u001b[0m"
else
  echo "\u001b[31mBUILD FAILED\u001b[0m"
fi
echo