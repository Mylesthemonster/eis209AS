import pyaudio
import time
import wave
import os.path


def uniquify(path):
    filename, extension = os.path.splitext(path)
    counter = 1

    while os.path.exists(path):
        path = filename + "_" + str(counter) + extension
        counter += 1

    return path

CHUNK = 1024  # How many audio saamples per frame
FORMAT = pyaudio.paInt16 # Bytes per sample
CHANNELS = 1 
RATE = 44100 # Samples per second 44.1 khz most common 
RECORD_SECONDS = 10
WAVE_OUTPUT_FILENAME = uniquify("test.wav")


print("Prepare to Record")
for i in range(3,0,-1):
    print(f'{i}\n')
    time.sleep(1)
    
print('GO')

p = pyaudio.PyAudio()

frames = []

stream = p.open(format = FORMAT,
                channels = CHANNELS, 
                rate = RATE, 
                input = True,
                frames_per_buffer = CHUNK)

print('****Recording****')

for i in range(0, int(RATE / CHUNK * RECORD_SECONDS)):
    data = stream.read(CHUNK)
    frames.append(data)
    
print('*****Finished****')

stream.stop_stream()
stream.close()
p.terminate()

wf = wave.open(WAVE_OUTPUT_FILENAME, 'wb')
wf.setnchannels(CHANNELS)
wf.setsampwidth(p.get_sample_size(FORMAT))
wf.setframerate(RATE)
wf.writeframes(b''.join(frames))
wf.close()