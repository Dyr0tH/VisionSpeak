import requests

def synthesize(text, voice):
    # Base URL of the API
    base_url = "http://localhost:5500/api/tts"
    
    # Define the parameters for the API request
    params = {
        'voice': voice,  # Voice format like 'espeak:en' or 'tts:hi'
        'text': text,    # Text to speak
        'cache': 'false' # Disable WAV cache
    }
    
    # Send the GET request to the API
    response = requests.get(base_url, params=params)
    
    # Check if the request was successful
    if response.status_code == 200:
        # Define the audio file path where the WAV will be saved
        audio_file_path = f"sample.mp3"  # Name based on the first 10 characters of text and language
        
        # Write the audio content to a WAV file
        with open(audio_file_path, 'wb') as audio_file:
            audio_file.write(response.content)
        
        print(f"Audio saved to: {audio_file_path}")
        return audio_file_path
    else:
        print(f"Error: {response.status_code}")
        return None


def fetch_opentts_voices():
    # OpenTTS API endpoint for voices
    url = "http://localhost:5500/api/voices"
    
    try:
        # Send GET request to fetch available voices
        response = requests.get(url)
        
        if response.status_code == 200:
            voices = response.json()
            
            # Display each voice and its details
            for voice_id, details in voices.items():
                print(f"Voice ID: {voice_id}")
                print(f"  Name: {details['name']}")
                print(f"  Gender: {details['gender']}")
                print(f"  Language: {details['language']}")
                print(f"  Locale: {details['locale']}")
                print(f"  TTS Name: {details['tts_name']}")
                print("-" * 40)
            
            return voices
        else:
            print(f"Error: Unable to fetch voices (Status Code: {response.status_code})")
            return None
    except Exception as e:
        print(f"Error: {e}")
        return None
