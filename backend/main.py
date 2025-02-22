from fastapi import FastAPI, UploadFile, Form, HTTPException
from fastapi.responses import FileResponse
import os
import ObjectIdentifier, TTS
import docsWriter

app = FastAPI()
TRANSLATED_FOLDER = "translated"
description = ""
LANGUAGE_MAP = {
"English": "en",
"Hindi": "hi",
"French": "fr",
"German": "de",
"Italian": "it",
"Tamil": "ta"
}

def get_image_description(image_path: str, language: str) -> str:
    """Fetches the description of the image using ObjectIdentifier."""
    try:
        return ObjectIdentifier.recogImage(image_path, language)
    except Exception as e:
        return "Unable to store or analyze the image."


def get_translated_audio(description: str, language: str):
    lang_code = LANGUAGE_MAP.get(language, "en")
    
    voice = f"flite:{lang_code}"
    TTS.synthesize(description, voice)


@app.post("/analyze")
async def analyze_image(image: UploadFile, language: str = Form(...)):
    
    global description

    image_path = "image.jpg"

    with open(image_path, 'wb') as imageFile:
        imageFile.write(await image.read())
    
    description = get_image_description(image_path, language)

    docsWriter.writeDocs(description, LANGUAGE_MAP)

    translated_files = [
        {"file_name": file, "download_url": f"download/{file}"} 
        for file in os.listdir(TRANSLATED_FOLDER) 
        if os.path.isfile(os.path.join(TRANSLATED_FOLDER, file))
    ]

    get_translated_audio(description, language)

    return {
        "description": description,
        "files": translated_files,
        "language": language,
        "audio_url": "audio/sample.mp3"
    }


# Serve audio file
@app.get("/audio/sample.mp3")
async def get_audio():
    return FileResponse("sample.mp3", media_type="audio/mpeg", filename="sample.mp3")

# Serve file downloads
@app.get("/download/{filename}")
async def download_file(filename: str):
    file_path = os.path.join(TRANSLATED_FOLDER, filename)

    if not os.path.exists(file_path):
        raise HTTPException(status_code=404, detail="File not found")

    content_type_map = {
        ".docx": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        ".txt": "text/plain",
        ".pdf": "application/pdf",
        ".mp3": "audio/mpeg",
        ".jpg": "image/jpeg",
        ".png": "image/png",
        ".zip": "application/zip",
    }
    
    ext = os.path.splitext(filename)[-1].lower()
    content_type = content_type_map.get(ext, "application/octet-stream")

    headers = {"Content-Disposition": f'attachment; filename="{filename}"'}

    return FileResponse(file_path, media_type=content_type, headers=headers)
