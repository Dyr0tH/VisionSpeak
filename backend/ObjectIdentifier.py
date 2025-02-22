import ollama
from PIL import Image

def preprocess_image(input_path, output_path="processed_image.jpg", size=(1024, 1024)):
    """Convert image to RGB JPEG and resize it for better compatibility."""
    img = Image.open(input_path)
    img = img.convert("RGB")  # Ensure image is in RGB mode
    img = img.resize(size)  # Resize to a standard dimension
    img.save(output_path, "JPEG")  # Save as JPEG format
    return output_path  # Return the new image path

def recogImage(imgPath, language):
    """Process the image and send it to Ollama for description."""
    processed_image = preprocess_image(imgPath)  # Convert & resize before sending

    response = ollama.chat(
        model='llama3.2-vision:11b',
        messages=[{
            'role': 'system',
            'content': 'Do not engage in any other conversation. Just provide a short but clear description of the image.'
        },
        {
            'role': 'user',
            'content': f'What is in this image? respond in {language} language. Do not give "*" or any other pretiffier for text. dont go deep into explanation, keep it simple',
            'images': [processed_image]  # Use the processed image
        }]
    )

    return response.message.content  # Extract only the description


def languageWriter(description, language_dict):
    responses = {}

    for language in language_dict.keys():

        response = ollama.chat(
            model='llama3.2-vision:11b',
            messages=[{
                'role': 'system',
                'content': 'Do not engage in any other conversation. Just provide a short but clear description of the image.'
            },
            {
                'role': 'user',
                'content': f'here is the description: {description}.Convert it into {language} language. keep the words which cannot be converted as they are and translate the rest.'
            }]
        )

        responses[language] = response.message.content

    return responses