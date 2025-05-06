set -e
mkdir -p ~/.gradle && touch ~/.gradle/gradle.properties
python -m pip install --upgrade pip
pip install fastapi==0.111 "uvicorn[standard]"==0.29.0
