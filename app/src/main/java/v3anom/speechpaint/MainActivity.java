package v3anom.speechpaint;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private TextView mText;
    private SpeechRecognizer sr;
    private static final String TAG = "MyStt3Activity";
    private TextView textView[];
    private TextView cc;
    private CanvasView canvasView;
    private TextToSpeech tts;
    private boolean coordinateGet;
    private boolean waitForIntent;
    private boolean utteranceRunning;
    private int coordinateCount;
    private float coords[];
    private char command;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        canvasView = (CanvasView) findViewById(R.id.canvas);
        canvasView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        //setContentView(canvasView);

        coords = new float[10];
        coordinateGet = false;
        waitForIntent = false;
        coordinateCount = 0;
        utteranceRunning = false;

        textView = new TextView[4];
        textView[0] = (TextView) findViewById(R.id.textView4);
        textView[1] = (TextView) findViewById(R.id.textView3);
        textView[2] = (TextView) findViewById(R.id.textView2);
        textView[3] = (TextView) findViewById(R.id.textView);
        mText = (TextView) findViewById(R.id.txtSpeechInput);
        cc = (TextView) findViewById(R.id.textView5);


        tts = new TextToSpeech(this, this);
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.ENGLISH);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
            //tts.setOnUtteranceProgressListener(this);
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) {
                    utteranceRunning = false;
                    Log.d("MainActivity", "TTS finished-"+utteranceId);
                }

                @Override
                public void onError(String utteranceId) {
                    utteranceRunning = false;
                    Log.d("MainActivity", "TTS Error-"+utteranceId);
                }

                @Override
                public void onStart(String utteranceId) {
                    utteranceRunning = true;
                    Log.d("MainActivity", "TTS started-"+utteranceId);
                }
            });
            speak("Hello");

        } else {
            Log.e("TTS", "Initialization Failed!");
        }
    }

    public  void clearCanvas(View v){
        canvasView.clearCanvas();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            Toast.makeText(this, "Volume Up Pressed", Toast.LENGTH_SHORT).show();
            speakUtteranceID("You pressed the input button, enter a command");
            promptSpeechInput("Enter a Command");
            return true;
        }
        else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            Toast.makeText(this, "Volume Down Pressed", Toast.LENGTH_SHORT);
            promptSpeechInput("Say Something...");
            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }


    class listener implements RecognitionListener {
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
        }
        public void onBeginningOfSpeech(){
            Log.d(TAG, "onBeginningOfSpeech");
        }
        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onRmsChanged");
        }
        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech");
        }
        public void onError(int error) {
            Log.d(TAG,  "error " +  error);
            mText.setText("error " + error);
            waitForIntent = false;
            if(coordinateGet && error == 7){
                waitForIntent = true;
                speakUtteranceID("Sorry I cannot understand the command please try again, or use cancel to leave");
                promptSpeechInput("Try again...");
            } else {
                speakUtteranceID("Sorry I cannot understand the command please press the input key again");
            }
        }
        public void onResults(Bundle results) {
            String str = new String();
            Log.d(TAG, "onResults " + results);
            ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if(data.size() != 0) {
                waitForIntent = false;
                for (int i = 0; i < data.size(); i++) {
                    Log.d(TAG, "result " + data.get(i));
                    str += " " + data.get(i);
                }
                mText.setText("Results: "+ str);
                if (coordinateGet) {
                    if (str.contains("cancel")) {
                        speakUtteranceID("Cancel command, press volume up to continue");
                        waitForIntent = false;
                        coordinateGet = false;
                        return;
                    }
                    float num = 0;
                    boolean numeric = false;
                    int i;
                    for (i = 0;i < data.size();i++) {
                        try {
                            num = Float.parseFloat(data.get(i).trim());
                            numeric = true;
                            break;
                        } catch (NumberFormatException e){
                            continue;
                        }
                    }
                    if (numeric) {
                        textView[coordinateCount].setText("" + num);
                        coords[coordinateCount++] = num;
                        cc.setText("" + coordinateCount);
                        drawShape(command);
                    }
                    else {
                        speakUtteranceID("Sorry I cannot understand the command please try again, or use cancel to leave");
                        promptSpeechInput("Try again...");
                    }
                } else {
                    if (str.contains("cancel")) {
                        speakUtteranceID("Cancel command, press volume up to continue");
                        command = 'x';
                        return;
                    } else if (str.contains("clear")) {
                        command = 's';
                        canvasView.clearCanvas();
                    } else if (str.contains("line")) {
                        command = 'l';
                        coordinateGet = true;
                        coordinateCount = 0;
                        drawShape('l');
                    } else if (str.contains("circle")) {
                        command = 'c';
                        coordinateGet = true;
                        coordinateCount = 0;
                        drawShape('c');
                    } else {
                        speakUtteranceID("Sorry I cannot understand the command please try again, or use cancel to leave");
                    }
                }
            } else {
                waitForIntent = true;
            }
        }
        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults");
        }
        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
    }

    private void promptSpeechInput(String text){
        waitForIntent = true;
        while(utteranceRunning) {
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) {
                    utteranceRunning = false;
                    Log.d("MainActivity", "TTS finished-"+utteranceId);
                }

                @Override
                public void onError(String utteranceId) {
                    utteranceRunning = false;
                    Log.d("MainActivity", "TTS Error-"+utteranceId);
                }

                @Override
                public void onStart(String utteranceId) {
                    utteranceRunning = true;
                    Log.d("MainActivity", "TTS started-"+utteranceId);
                }
            });
        }
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        //i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000 );
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, text);
        try {
            //startActivityForResult(i, 100);
            sr.startListening(i);
            Log.i("111111","11111111");
        } catch (ActivityNotFoundException a) {
            Toast.makeText(MainActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
        }
    }

    private void speakUtteranceID(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            utteranceRunning = true;
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
        }else{
            utteranceRunning = true;
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void speak(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            utteranceRunning = true;
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);

        }else{
            utteranceRunning = true;
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void drawShape(final char shape) {
        switch (shape) {
            case 'l':
                if (!mText.getText().toString().contains("error")) {
                    if (coordinateCount == 0 && !waitForIntent) {
                        speakUtteranceID("Enter X 1 Coordinate");
                        promptSpeechInput("Enter X1 Coordinate");
                        drawShape(shape);
                    } else if (coordinateCount == 1 && !waitForIntent) {
                        speakUtteranceID("Enter Y 1 Coordinate");
                        promptSpeechInput("Enter Y1 Coordinate");
                        drawShape(shape);
                    } else if (coordinateCount == 2 && !waitForIntent) {
                        speakUtteranceID("Enter X 2 Coordinate");
                        promptSpeechInput("Enter X2 Coordinate");
                        drawShape(shape);
                    } else if (coordinateCount == 3 && !waitForIntent) {
                        speakUtteranceID("Enter Y 2 Coordinate");
                        promptSpeechInput("Enter Y2 Coordinate");
                        drawShape(shape);
                    } else if (coordinateCount == 4 && !waitForIntent) {
                        coordinateGet = false;
                        canvasView.drawLine(coords[0], coords[1], coords[2], coords[3]);
                    } else {
                        Log.e("DRAW", "Error in coordinate system or intent busy");
                        //coordinateCount = 0;
                    }
                } else {
                    drawShape(shape);
                }
                break;
            case 'c':
                if (!mText.getText().toString().contains("error")) {
                    if (coordinateCount == 0 && !waitForIntent) {
                        speakUtteranceID("Enter Center X Coordinate");
                        promptSpeechInput("Enter Center X Coordinate");
                        drawShape(shape);
                    } else if (coordinateCount == 1 && !waitForIntent) {
                        speakUtteranceID("Enter Center Y Coordinate");
                        promptSpeechInput("Enter Center Y Coordinate");
                        drawShape(shape);
                    } else if (coordinateCount == 2 && !waitForIntent) {
                        speakUtteranceID("Enter Radius");
                        promptSpeechInput("Enter Radius");
                        drawShape(shape);
                    } else if (coordinateCount == 3 && !waitForIntent) {
                        coordinateGet = false;
                        canvasView.drawCircle(coords[0], coords[1], coords[2]);
                    } else {
                        Log.e("DRAW", "Error in coordinate system or intent busy");
                        //coordinateCount = 0;
                    }
                } else {
                    drawShape(shape);
                }
                break;
        }
    }
}
