package dev.noash.applocker;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Button BTN1;
    private Button BTN2;
    private Button BTN3;
    private Button BTN4;
    private Button BTN5;
    private static final int REQUEST_CONTACT_PERMISSION = 1;
    private static final int REQUEST_SMS_PERMISSION = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 123;
    private final String TARGET_PHONE = "0540000000";
    private boolean isListening = false;
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private Uri photoUri;
    private String lastGeneratedCode = null;
    MediaPlayer mp;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        findViews();
        initViews();
    }
    private void findViews() {
        BTN1=findViewById(R.id.circle1);
        BTN2=findViewById(R.id.circle2);
        BTN3=findViewById(R.id.circle3);
        BTN4=findViewById(R.id.circle4);
        BTN5=findViewById(R.id.circle5);
        mp = MediaPlayer.create(this, R.raw.claps);
    }
    private void initViews() {
        BTN1.setOnClickListener(v -> batteryGuessDialog());
        BTN2.setOnClickListener(v -> checkContactDialog());
        BTN3.setOnClickListener(v -> cameraTaskDialog());
        BTN4.setOnClickListener(v -> sendCodeDialog());
        BTN5.setOnClickListener(v -> voiceChallengeDialog());
        BTN2.setClickable(false);
        BTN3.setClickable(false);
        BTN4.setClickable(false);
        BTN5.setClickable(false);
        mp.setOnCompletionListener(mp -> {
            mp.release();
            endGame();
        });
    }
    private void checkPermissions() {
        //contacts
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_CONTACT_PERMISSION);
        }
        //camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    202); // מזהה לבחירתך
        }
        //sms
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS},
                    REQUEST_SMS_PERMISSION);
        }
        //record
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    101); // או כל מספר מזהה
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "he-IL"); // עברית
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CONTACT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                findContactByPhone();
            } else {
                Toast.makeText(this, "נדרשת הרשאה לגשת לאנשי הקשר", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "הרשאת SMS אושרה", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "נדרשת הרשאת SMS כדי להמשיך", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void changeCircleColor(View button, int color) {
        GradientDrawable bg = (GradientDrawable) button.getBackground();
        bg.setColor(color);
    }
    private void activeNextStage(Button current, Button next) {
        current.setClickable(false);
        next.setClickable(true);
    }
    private void batteryGuessDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("הכנס אחוז סוללה בטלפון (0-100)");

        new AlertDialog.Builder(this)
                .setTitle("משימה 1 : בדיקת סוללה")
                .setView(input)
                .setPositiveButton("בדיקה", (dialog, which) -> checkBatteryLevel(input.getText().toString()))
                .setNegativeButton("ביטול", null)
                .show();
    }
    private void checkBatteryLevel(String userInputStr) {
        try {
            int userInput = Integer.parseInt(userInputStr);
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, ifilter);

            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int batteryPct = (int) ((level / (float) scale) * 100);

                if (userInput == batteryPct) {
                    Toast.makeText(MainActivity.this, "משימה 1 הושלמה בהצלחה", Toast.LENGTH_SHORT).show();
                    changeCircleColor(BTN1, Color.GREEN);
                    activeNextStage(BTN1, BTN2);
                } else {
                    Toast.makeText(MainActivity.this, "למה לשקר", Toast.LENGTH_SHORT).show();
                    changeCircleColor(BTN1, Color.RED);
                }
            }
        } catch (NumberFormatException e) {
            Toast.makeText(MainActivity.this, "פורמט לא תקין", Toast.LENGTH_SHORT).show();
        }
    }
    private void checkContactDialog() {
        new AlertDialog.Builder(this)
                .setTitle("משימה 2 : אנשי קשר")
                .setMessage("יש להוסיף את מספר הטלפון של מפתחת האפליקציה לאנשי הקשר. במידה וקיים לחץ על כפתור 'בדיקה'.")
                .setPositiveButton("בדיקה", (dialog, which) -> findContactByPhone())
                .setNegativeButton("ביטול", null)
                .show();
    }
    private void findContactByPhone() {
        ContentResolver cr = getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor cursor = cr.query(uri, projection, null, null, null);
        boolean found = false;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String number = cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                number = number.replaceAll("[^0-9]", "");

                if (number.endsWith(TARGET_PHONE)) {
                    found = true;
                    break;
                }
            }
            cursor.close();
        }

        if (found) {
            Toast.makeText(MainActivity.this, "משימה 2 הושלמה בהצלחה", Toast.LENGTH_SHORT).show();
            changeCircleColor(BTN2, Color.GREEN);
            activeNextStage(BTN2, BTN3);
        } else {
            Toast.makeText(MainActivity.this, "המספר לא נמצא. אנא נסה שנית", Toast.LENGTH_SHORT).show();
            changeCircleColor(BTN2, Color.RED);
        }
    }
    private void cameraTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("משימה 3 : מצלמה");
        builder.setMessage("צלם פריט שצבעו אדום באופן דומיננטי. \nלחץ על כפתור המצלמה כדי להתחיל.");
        builder.setPositiveButton("פתח מצלמה", (dialog, which) -> dispatchTakePictureIntent());
        builder.setNegativeButton("ביטול", null);
        builder.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoUri = createImageUri();

        if (photoUri != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "לא ניתן לשמור תמונה", Toast.LENGTH_SHORT).show();
        }
    }
    private Uri createImageUri() {
        long timestamp = System.currentTimeMillis();
        String fileName = "photo_" + timestamp + ".jpg";

        String tsStr = String.valueOf(timestamp);
        lastGeneratedCode = tsStr.substring(tsStr.length() - 4);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/AppLocker");

        ContentResolver resolver = getContentResolver();
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                analyzeRedColorInImage(imageBitmap); // ממשיך לניתוח צבעים
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "שגיאה בטעינת תמונה", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void analyzeRedColorInImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int redPixels = 0;
        int totalPixels = width * height;

        for (int x = 0; x < width; x += 5) {
            for (int y = 0; y < height; y += 5) {
                int pixel = bitmap.getPixel(x, y);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                if (red > 150 && red > green + 30 && red > blue + 30) {
                    redPixels++;
                }
            }
        }

        float redRatio = (float) redPixels / (totalPixels / 25);

        if (redRatio > 0.4) { // לפחות 40% מהתמונה באדום
            changeCircleColor(BTN3, Color.GREEN);
            activeNextStage(BTN3, BTN4);
            Toast.makeText(this, "משימה 4 הושלמה בהצלחה", Toast.LENGTH_SHORT).show();
        } else {
            changeCircleColor(BTN3, Color.RED);
            Toast.makeText(this, "הצבע האדום לא דומיננטי מספיק. אנא נסה שנית", Toast.LENGTH_SHORT).show();
        }
    }
    private void sendCodeDialog() {
        String message = "שלח את הקוד הבא למספר הטלפון שהוספת בשלב 2:  \n\n";
        message += "הקוד שלך: " + lastGeneratedCode;

        new AlertDialog.Builder(this)
                .setTitle("משימה 4 : שליחת קוד")
                .setMessage(message)
                .setPositiveButton("בדיקה", (dialog, which) -> checkIfUserSentCode())
                .setNegativeButton("ביטול", null)
                .show();
    }
    private void checkIfUserSentCode() {
        ContentResolver cr = getContentResolver();
        Uri uri = Uri.parse("content://sms/sent");

        Cursor cursor = cr.query(uri, null, null, null, "date DESC");

        boolean found = false;

        if (cursor != null) {
            int addressIndex = cursor.getColumnIndex("address");
            int bodyIndex = cursor.getColumnIndex("body");

            while (cursor.moveToNext()) {
                String number = cursor.getString(addressIndex).replaceAll("[^0-9]", "");
                String body = cursor.getString(bodyIndex);

                if (number.endsWith(TARGET_PHONE) && body.contains(lastGeneratedCode)) {
                    found = true;
                    break;
                }
            }
            cursor.close();
        }

        if (found) {
            changeCircleColor(BTN4, Color.GREEN);
            activeNextStage(BTN4, BTN5);
            Toast.makeText(this, "משימה 4 הושלמה בהצלחה", Toast.LENGTH_SHORT).show();
        } else {
            changeCircleColor(BTN4, Color.RED);
            Toast.makeText(this, "קוד שגוי. אנא נסה שנית", Toast.LENGTH_SHORT).show();
        }
    }
    private void voiceChallengeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_speak_task, null);
        builder.setView(dialogView);
        builder.setCancelable(true);

        ImageView ivMic = dialogView.findViewById(R.id.iv_mic);
        TextView tvInstruction = dialogView.findViewById(R.id.tv_instruction);

        AlertDialog dialog = builder.create();
        dialog.setTitle("משימה 5 : דיבור");
        dialog.setMessage("לחץ על המיקרופון כדי להתחיל");
        dialog.show();

        ivMic.setOnClickListener(view -> {
            if (!isListening) {
                startListening();
                tvInstruction.setText("האזנה פעילה... לחץ שוב לסיום");
                isListening = true;
            } else {
                stopListening();
                tvInstruction.setText("בודק את מה שנאמר...");
                isListening = false;
                dialog.dismiss();
            }
        });
    }
    private void startListening() {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onError(int error) {
                changeCircleColor(BTN5, Color.RED);
                Toast.makeText(MainActivity.this, "שגיאה בזיהוי דיבור", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null) {
                    for (String result : matches) {
//                        Log.d("SpeechResult", "תוצאה: " + result);
//                        Toast.makeText(MainActivity.this, "שמענו: " + result, Toast.LENGTH_LONG).show();

                        String normalized = result.toLowerCase().replaceAll("[^א-ת ]", "");

                        if (
                                normalized.contains("שומשום היפתח") ||
                                        normalized.contains("שומשום יפתח") ||
                                        normalized.contains("שומשום נפתח") ||
                                        normalized.contains("שום שום היפתח") ||
                                        normalized.contains("סום סום יפתח") ||
                                        ((normalized.contains("שומשום") || normalized.contains("סומסום")) && (normalized.contains("היפתח") || normalized.contains("יפתח") || normalized.contains("נפתח")))
                        ) {
                            changeCircleColor(BTN5, Color.GREEN);
                            Toast.makeText(MainActivity.this, "משימה 5 הושלמה בהצלחה", Toast.LENGTH_SHORT).show();
                            mp.start();
                            return;
                        }
                    }
                }

                changeCircleColor(BTN5, Color.RED);
                Toast.makeText(MainActivity.this, "ניסיון לא הצליח. אנא נסה שנית", Toast.LENGTH_SHORT).show();
            }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizer.startListening(speechIntent);
    }
    private void stopListening() {
        speechRecognizer.stopListening();
    }
    private void endGame() {
        Intent i = new Intent(this, HomeActivity.class);
        Bundle bundle = new Bundle();
        i.putExtras(bundle);
        startActivity(i);
        onPause();
    }
}
