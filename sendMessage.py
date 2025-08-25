import requests
import time
import os


def send_signal_message(text):
    params = {
        'phone': os.environ.get('SIGNAL_PHONE'),
        'apikey': os.environ.get('SIGNAL_APIKEY'),
        'text': text
    }
    response = requests.get('https://signal.callmebot.com/signal/send.php', params=params)
    return response.status_code == 200

def send_file_in_chunks(filename, chunk_size=1500):
    with open(filename, 'r', encoding='utf-8') as file:
        content = file.read()
    
    # Split content into chunks
    chunks = [content[i:i+chunk_size] for i in range(0, len(content), chunk_size)]
    
    for i, chunk in enumerate(chunks, 1):
        message = f"Part {i}/{len(chunks)}:\n{chunk}"
        print(f"Sending part {i}...")
        if send_signal_message(message):
            print(f"Part {i} sent successfully")
        else:
            print(f"Failed to send part {i}")
        time.sleep(2)  # Wait between messages

send_file_in_chunks('redeploy_report.txt')
