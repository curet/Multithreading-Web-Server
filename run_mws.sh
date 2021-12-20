echo "[sh] Compiling Code..."
javac MultithreadingWebServer.java
echo "[sh] Executing Bycode..."
echo "[sh] Server Active!"
java MultithreadingWebServer
echo "[sh] Server/Client Connection Ended."
rm Client.class MultithreadingWebServer.class
