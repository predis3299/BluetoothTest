package ssu.bluetoothtest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    String TAG = "MainActivity";
    UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    TextView textStatus;
    Button btnParied, btnSearch, btnSend;
    ListView listView;
    EditText txtText;

    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> btArrayAdapter;
    ArrayList<String> deviceAddressArray;

    private TextToSpeech tts;

    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothSocket btSocket = null;
    ConnectedThread connectedThread;

    private int[][] exlist3 = {{1, 1, 1, 0, 0, 0}, {0, 0, 0, 1, 0, 0}, {1, 1, 1, 0, 0, 1}};
    private int[][] exlist6 = {{1, 1, 1, 0, 0, 0}, {0, 0, 0, 1, 0, 0}, {1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 0}, {0, 0, 0, 1, 0, 0}, {1, 1, 1, 0, 0, 1}};
    private int[][] exlist2 = {{1, 1, 1, 0, 0, 0}, {0, 0, 0, 1, 0, 0}};
    private int[][] exlist9 = {{1, 1, 1, 0, 0, 0}, {0, 0, 0, 1, 0, 0}, {1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 0}, {0, 0, 0, 1, 0, 0}, {1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 0}, {0, 0, 0, 1, 0, 0}, {1, 1, 1, 0, 0, 1}};
    private int[][] exlist1 = {{1, 1, 1, 0, 0, 0}};
    private int[][] exlist4 = {{1, 1, 1, 0, 0, 0}, {0, 0, 0, 1, 0, 0}, {1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 0}};
    private int[][] exlist5 = {{1, 1, 1, 0, 0, 0}, {0, 0, 0, 1, 0, 0}, {1, 1, 1, 0, 0, 1}, {1, 1, 1, 0, 0, 0}, {0, 0, 0, 1, 0, 0}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get permission
        String[] permission_list = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(MainActivity.this, permission_list, 1);

        // Enable bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // variables
        textStatus = (TextView) findViewById(R.id.text_status);
        btnParied = (Button) findViewById(R.id.btn_paired);
        btnSearch = (Button) findViewById(R.id.btn_search);
        btnSend = (Button) findViewById(R.id.btn_send);
        txtText = findViewById(R.id.txtText);
        listView = (ListView) findViewById(R.id.listview);

        //tts init
        tts = new TextToSpeech(this, this);

        // Show paired devices
        btArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceAddressArray = new ArrayList<>();
        listView.setAdapter(btArrayAdapter);

        listView.setOnItemClickListener(new myOnItemClickListener());

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence CharSeq = txtText.getText();
                String text = CharSeq.toString();

                ArrayList<Object> Result = text(text);

                Log.d("braille", Arrays.deepToString(Result.toArray()));
                Log.d("length", String.valueOf(Result.size()));

                String row = String.valueOf(Result.size());
                String braille = Arrays.deepToString(Result.toArray());
                braille = row + braille + ";";  //끝에 ; 추가

                Log.d("strResult", braille);
                if(connectedThread!=null){ connectedThread.write(braille); }



                /*
                // test
                String braille = null;
                int rowCnt = 0;

                if(text.equals("1")) {
                    braille = Arrays.deepToString(exlist1);
                    rowCnt = exlist1.length;
                }
                else if(text.equals("2")) {
                    braille = Arrays.deepToString(exlist2);
                    rowCnt = exlist2.length;
                }
                else if(text.equals("3")) {
                    braille = Arrays.deepToString(exlist3);
                    rowCnt = exlist3.length;
                }
                else if(text.equals("6")) {
                    braille = Arrays.deepToString(exlist6);
                    rowCnt = exlist6.length;
                }
                else if(text.equals("9")) {
                    braille = Arrays.deepToString(exlist9);
                    rowCnt = exlist9.length;
                }
                else if(text.equals("4")) {
                    braille = Arrays.deepToString(exlist4);
                    rowCnt = exlist4.length;
                }
                else if(text.equals("5")) {
                    braille = Arrays.deepToString(exlist5);
                    rowCnt = exlist5.length;
                }

                Log.d("braille", braille);
                String row = String.valueOf(rowCnt);
                braille = row + braille + ";";
                Log.d("send", braille);

                if(connectedThread!=null){
                    connectedThread.write(braille); }
                */
            }
        });
    }

    // 모든 초성에 대한 점자를 매치해놓은 HashMap 변수 선언 (파이썬의 딕셔너리와 비슷)

    public static final HashMap<String , int[][]> MATCH_H2B_CHO = new HashMap<String, int[][]>() {{
        put("ㄱ", new int[][] {{0,0,0,1,0,0}});
        put("ㄴ", new int[][] {{1,0,0,1,0,0}});
        put("ㄷ", new int[][] {{0,1,0,1,0,0}});
        put("ㄹ", new int[][] {{0,0,0,0,1,0}});
        put("ㅁ", new int[][] {{1,0,0,0,1,0}});
        put("ㅂ", new int[][] {{0,0,0,1,1,0}});
        put("ㅅ", new int[][] {{0,0,0,0,0,1}});
        put("ㅇ", new int[][] {{1,1,0,1,1,0}});
        put("ㅈ", new int[][] {{0,0,0,1,0,1}});
        put("ㅊ", new int[][] {{0,0,0,0,1,1}});
        put("ㅋ", new int[][] {{1,1,0,1,0,0}});
        put("ㅌ", new int[][] {{1,1,0,0,1,0}});
        put("ㅍ", new int[][] {{1,0,0,1,1,0}});
        put("ㅎ", new int[][] {{0,1,0,1,1,0}});
        put("ㄲ", new int[][] {{0,0,0,0,0,1}, {0,0,0,1,0,0}});
        put("ㄸ", new int[][] {{0,0,0,0,0,1}, {0,1,0,1,0,0}});
        put("ㅃ", new int[][] {{0,0,0,0,0,1}, {0,0,0,1,1,0}});
        put("ㅆ", new int[][] {{0,0,0,0,0,1}, {0,0,0,0,0,1}});
        put("ㅉ", new int[][] {{0,0,0,0,0,1}, {0,0,0,1,0,1}});
    }};

    // 모든 중성에 대한 점자를 매치해놓은 HashMap 변수 선언
    public static final Map<String , int[][]> MATCH_H2B_JOONG = new HashMap<String, int[][]>() {{
        put("ㅏ", new int[][]{{1,1,0,0,0,1}});
        put("ㅑ", new int[][]{{0,0,1,1,1,0}});
        put("ㅓ", new int[][]{{0,1,1,1,0,0}});
        put("ㅕ", new int[][]{{1,0,0,0,1,1}});
        put("ㅗ", new int[][]{{1,0,1,0,0,1}});
        put("ㅛ", new int[][]{{0,0,1,1,0,1}});
        put("ㅜ", new int[][]{{1,0,1,1,0,0}});
        put("ㅠ", new int[][]{{1,0,0,1,0,1}});
        put("ㅡ", new int[][]{{0,1,0,1,0,1}});
        put("ㅣ", new int[][]{{1,0,1,0,1,0}});
        put("ㅐ", new int[][]{{1,1,1,0,1,0}});
        put("ㅔ", new int[][]{{1,0,1,1,1,0}});
        put("ㅒ", new int[][]{{0,0,1,1,1,0}, {1,1,1,0,1,0}});
        put("ㅖ", new int[][]{{0,0,1,1,0,0}});
        put("ㅘ", new int[][]{{1,1,1,0,0,1}});
        put("ㅙ", new int[][]{{1,1,1,0,0,1}, {1,1,1,0,1,0}});
        put("ㅚ", new int[][]{{1,0,1,1,1,1}});
        put("ㅝ", new int[][]{{1,1,1,1,0,0}});
        put("ㅞ", new int[][]{{1,1,1,1,0,0}, {1,1,1,0,1,0}});
        put("ㅟ", new int[][]{{1,0,1,1,0,0}, {1,1,1,0,1,0}});
        put("ㅢ", new int[][]{{0,1,0,1,1,1}});
    }};

    // 모든 종성에 대한 점자를 매치해놓은 HashMap 변수 선언
    private static final Map <String, int[][]> MATCH_H2B_JONG = new HashMap<String, int[][]>() {{
        put("ㄱ", new int[][]{{1, 0, 0, 0, 0, 0}});
        put("ㄴ", new int[][]{{0, 1, 0, 0, 1, 0}});
        put("ㄷ", new int[][]{{0, 0, 1, 0, 1, 0}});
        put("ㄹ", new int[][]{{0, 1, 0, 0, 0, 0}});
        put("ㅁ", new int[][]{{0, 1, 0, 0, 0, 1}});
        put("ㅂ", new int[][]{{1, 1, 0, 0, 0, 0}});
        put("ㅅ", new int[][]{{0, 0, 1, 0, 0, 0}});
        put("ㅇ", new int[][]{{0, 1, 1, 0, 1, 1}});
        put("ㅈ", new int[][]{{1, 0, 1, 0, 0, 0}});
        put("ㅊ", new int[][]{{0, 1, 1, 0, 0, 0}});
        put("ㅋ", new int[][]{{0, 1, 1, 0, 1, 0}});
        put("ㅌ", new int[][]{{0, 1, 1, 0, 0, 1}});
        put("ㅍ", new int[][]{{0, 1, 0, 0, 1, 1}});
        put("ㅎ", new int[][]{{0, 0, 1, 0, 1, 1}});
        put("ㄲ", new int[][]{{1, 0, 0, 0, 0, 0}, {1, 0, 0, 0, 0, 0}});
        put("ㄳ", new int[][]{{1, 0, 0, 0, 0, 0}, {0, 0, 1, 0, 0, 0}});
        put("ㄵ", new int[][]{{0, 1, 0, 0, 1, 0}, {1, 0, 1, 0, 0, 0}});
        put("ㄶ", new int[][]{{0, 1, 0, 0, 1, 0}, {0, 0, 1, 0, 1, 1}});
        put("ㄺ", new int[][]{{0, 1, 0, 0, 0, 0}, {1, 0, 0, 0, 0, 0}});
        put("ㄻ", new int[][]{{0, 1, 0, 0, 0, 0}, {0, 1, 0, 0, 0, 1}});
        put("ㄼ", new int[][]{{0, 1, 0, 0, 0, 0}, {1, 1, 0, 0, 0, 0}});
        put("ㄽ", new int[][]{{0, 1, 0, 0, 0, 0}, {0, 0, 1, 0, 0, 0}});
        put("ㄾ", new int[][]{{0, 1, 0, 0, 0, 0}, {0, 1, 1, 0, 0, 1}});
        put("ㄿ", new int[][]{{0, 1, 0, 0, 0, 0}, {0, 1, 0, 0, 1, 1}});
        put("ㅀ", new int[][]{{0, 1, 0, 0, 0, 0}, {0, 0, 1, 0, 1, 1}});
        put("ㅄ", new int[][]{{1, 1, 0, 0, 0, 0}, {0, 0, 1, 0, 0, 0}});
    }};

    // 문법에 규정된 약어/약자 점자를 매치해놓은 HashMap 변수 선언
    public static final Map<String, int[][]> MATCH_H2B_GRAMMAR = new HashMap<String, int[][]>() {{
        put("ㄱ", new int[][]{{1, 1, 0, 1, 0, 1}});  // 'ㄱ'+'ㅏ' 일 때 'ㄱㅏ'에 해당하는 점자를 의미
        put("ㄴ", new int[][]{{1, 0, 0, 1, 0, 0}});
        put("ㄷ", new int[][]{{0, 1, 0, 1, 0, 0}});
        put("ㅁ", new int[][]{{1, 0, 0, 0, 1, 0}});
        put("ㅂ", new int[][]{{0, 0, 0, 1, 1, 0}});
        put("ㅅ", new int[][]{{1, 1, 1, 0, 0, 0}});
        put("ㅈ", new int[][]{{0, 0, 0, 1, 0, 1}});
        put("ㅋ", new int[][]{{1, 1, 0, 1, 0, 0}});
        put("ㅌ", new int[][]{{1, 1, 0, 0, 1, 0}});
        put("ㅍ", new int[][]{{1, 0, 0, 1, 1, 0}});
        put("ㅎ", new int[][]{{0, 1, 0, 1, 1, 0}});

        put("것", new int[][]{{0, 0, 0, 1, 1, 1}, {0, 1, 1, 1, 0, 0}}); // '것'이라는 글자에 해당하는 점자를 의미

        put("ㅆ", new int[][]{{0, 0, 1, 1, 0, 0}}); // 종성'ㅆ'에 해당하는 점자를 의미
    }};

    // 문법에 규정된 약어/약자 점자를 매치해놓은 HashMap 변수 선언
    // Key값에 String형이 들어가서 GRAMMAR2를 따로 만듦
    public static final Map<String, int[][]> MATCH_H2B_GRAMMAR2 = new HashMap<String, int[][]>() {{
        put("ㅓㄱ", new int[][] {{1,0,0,1,1,1}}); // '초성'+'ㅓ'+'ㄱ' 일 때 'ㅓㄱ'에 해당하는 점자를 의미
        put("ㅓㄴ", new int[][] {{0,1,1,1,1,1}});
        put("ㅓㄹ", new int[][] {{0,1,1,1,1,0}});
        put("ㅕㄴ", new int[][] {{1,0,0,0,0,1}});
        put("ㅕㄹ", new int[][] {{1,1,0,0,1,1}});
        put("ㅕㅇ", new int[][] {{1,1,0,1,1,1}});
        put("ㅗㄱ", new int[][] {{1,0,1,1,0,1}});
        put("ㅗㄴ", new int[][] {{1,1,1,0,1,1}});
        put("ㅗㅇ", new int[][] {{1,1,1,1,1,1}});
        put("ㅜㄴ", new int[][] {{1,1,0,1,1,0}});
        put("ㅜㄹ", new int[][] {{1,1,1,1,0,1}});
        put("ㅡㄴ", new int[][] {{1,0,1,0,1,1}});
        put("ㅡㄹ", new int[][] {{0,1,1,1,0,1}});
        put("ㅣㄴ", new int[][] {{1,1,1,1,1,0}});

    }};
    ////////////////////////////////////////////////////////


    ////// 한글 초성 중성 종성 분리하는 코드에 쓰일 변수 선언 //////
    private static final int BASE_CODE = 44032;
    private static final int CHOSUNG = 588;
    private static final int JUNGSUNG = 28;
    // 초성 리스트. 00 ~ 18
    private static final char[] CHOSUNG_LIST = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
            'ㅅ', 'ㅆ', 'ㅇ' , 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };
    // 중성 리스트. 00 ~ 20
    private static final char[] JUNGSUNG_LIST = {
            'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ',
            'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ',
            'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ',
            'ㅡ', 'ㅢ', 'ㅣ'
    };
    // 종성 리스트. 00 ~ 27 + 1(1개 없음)
    private static final char[] JONGSUNG_LIST = {
            ' ', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ',
            'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ',
            'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ',
            'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };
    ///////////////////////////////////////////////////






    // '안'이 들어오면 'ㅇㅏㄴ'으로 쪼갠 후 각각의 초성 중성 종성에 맞는 점자를 찾아 모두 ArrayList에 담아 리턴하는 함수
    // 217번줄부터 보면 더 좋아효!
    private static ArrayList<Object> letter(String HangulLetter) {
        ArrayList<Object> result = new ArrayList<Object>();


        // 문법: "것"이라는 글자는 초성중성종성 분리 전에 MATCH_H2B_GRAMMAR에서 점자 매치하여 리턴
        if (HangulLetter.equals("것")) {
            int[][] GrammarArray = MATCH_H2B_GRAMMAR.get(HangulLetter);
            for (int j = 0; j < GrammarArray.length; j++) {
                result.add(GrammarArray[j]);
            }
        }


        //////한글 초성 중성 종성 분리하는 코드 ex) "걱" => "ㄱㅓㄱ" ///////
        StringBuilder hangul_decomposed = new StringBuilder();
        char charTemp = HangulLetter.charAt(0);
        int cBase = charTemp - BASE_CODE;
        int c1 = cBase / CHOSUNG;
        hangul_decomposed.append(CHOSUNG_LIST[c1]);
        int c2 = (cBase - (CHOSUNG * c1)) / JUNGSUNG;
        hangul_decomposed.append(JUNGSUNG_LIST[c2]);
        int c3 = (cBase - (CHOSUNG * c1) - (JUNGSUNG * c2));
        hangul_decomposed.append(JONGSUNG_LIST[c3]);
        /////////////////////////////////////////////////////////////


        // 문법: MATCH_H2B_GRAMMAR2에 해당하는 중성+종성을 가진 글자는 MATCH_H2B_GRAMMAR2 에서 점자 매치하여 리턴 ex) 'ㄱ'+'ㅓ'+'ㄱ'는 'ㄱ' + 'ㅓㄱ'에 해당하는 두개의 점자로 줄여서 출력 가능함
        if (hangul_decomposed.length() >= 3 && MATCH_H2B_GRAMMAR2.containsKey(hangul_decomposed.substring(1, 3))) {
            if (!(hangul_decomposed.charAt(0) == 'ㅇ') && MATCH_H2B_CHO.containsKey(String.valueOf(hangul_decomposed.charAt(0))) ){
                int[][] ChoArray = MATCH_H2B_CHO.get(String.valueOf(hangul_decomposed.charAt(0)));
                for (int j = 0; j < ChoArray.length; j++) {
                    result.add(ChoArray[j]);
                }
            }
            int[][] GrammarArray = MATCH_H2B_GRAMMAR2.get(hangul_decomposed.substring(1, 3));
            for (int j = 0; j < GrammarArray.length; j++) {
                result.add(GrammarArray[j]);
            }
            return result;
        }


        // 문법: 중성에 'ㅏ'가 오는 글자는 초성을 무시하고 MATCH_H2B_GRAMMAR 에서 점자 매치하여 리턴 ex) 'ㄱ'+'ㅏ' = '가'로 합쳐서 하나의 점자로 리턴
        else if (hangul_decomposed.charAt(0) != 'ㅇ' && hangul_decomposed.charAt(0) != 'ㄹ' && hangul_decomposed.charAt(0) != 'ㅊ' && hangul_decomposed.charAt(1) == 'ㅏ') {
            //result.add(hangul_decomposed.charAt(0) + "ㅏ");
            int[][] GrammarArray = MATCH_H2B_GRAMMAR.get(String.valueOf(hangul_decomposed.charAt(0)));
            for (int j = 0; j < GrammarArray.length; j++) {
                result.add(GrammarArray[j]);
            }

            if (hangul_decomposed.length() >= 3 && MATCH_H2B_JONG.containsKey( String.valueOf(hangul_decomposed.charAt(2))) ) {
                int[][] JongArray = MATCH_H2B_JONG.get(String.valueOf(hangul_decomposed.charAt(2)));
                for (int j = 0; j < JongArray.length; j++) {
                    result.add(JongArray[j]);
                }
            }
            return result;
        }


        // 문법: 'ㅅ/ㅆ/ㅈ/ㅉ/ㅊ' + 'ㅓ' + 'ㅇ' 의 글자에서 'ㅓ+ㅇ'은 MATCH_H2B_GRAMMAR2 에서 'ㅕㅇ'에 해당하는 점자 매치하여 리턴  ex)'ㅅ/ㅆ/ㅈ/ㅉ/ㅊ'+'ㅓ'+'ㅇ'일때 ㅓ+ㅇ은 ㅕ+ㅇ으로 표기한다
        else if (hangul_decomposed.charAt(0) == 'ㅅ' || hangul_decomposed.charAt(0) == 'ㅆ' || hangul_decomposed.charAt(0) == 'ㅈ' || hangul_decomposed.charAt(0) == 'ㅉ' || hangul_decomposed.charAt(0) == 'ㅊ' && hangul_decomposed.charAt(1) == 'ㅓ' && hangul_decomposed.charAt(2) == 'ㅇ') {
            int[][] ChoArray = MATCH_H2B_CHO.get(String.valueOf(hangul_decomposed.charAt(0)));
            for (int j = 0; j < ChoArray.length; j++) {
                result.add(ChoArray[j]);
            }
            int[][] GrammarArray = MATCH_H2B_GRAMMAR2.get("ㅕㅇ");
            for (int j = 0; j < GrammarArray.length; j++) {
                result.add(GrammarArray[j]);
            }
            return result;
        }


        // 그 외 모든 나머지 글자에 대해서 초성,중성,종성 각각에 해당하는 점자 찾아서 리턴
        else {
            for (int i = 0; i < hangul_decomposed.length(); i++) {
                char hangul = hangul_decomposed.charAt(i);

                // 초성에 해당하는 점자를 MATCH_H2B_CHO(hashmap)에서 찾아서 result 리스트에 추가
                // 문법: 초성 'ㅇ'은 표기하지않는다.
                if (i == 0 && !(hangul == 'ㅇ') && MATCH_H2B_CHO.containsKey(String.valueOf(hangul))){
                    int[][] ChoArray = MATCH_H2B_CHO.get(String.valueOf(hangul));
                    for (int j = 0; j < ChoArray.length; j++) {
                        result.add(ChoArray[j]);
                    }

                }

                // 중성에 해당하는 점자 MATCH_H2B_JOONG(hashmap)애서 찾아서 result 리스트에 추가
                if (i == 1 && MATCH_H2B_JOONG.containsKey(String.valueOf(hangul))) {
                    System.out.println(hangul);
                    int[][] JoongArray = MATCH_H2B_JOONG.get(String.valueOf(hangul));
                    for (int j = 0; j < JoongArray.length; j++) {
                        result.add(JoongArray[j]);
                    }
                }

                // 문법: 종성에 있는 'ㅆ'은 약어가 존재한다. MATCH_H2B_GRAMMAR에서 점자 찾기 (초성에선 점자 두글자로 출력되지만 종성에 올땐 한글자로 출력)
                if (i == 2 && hangul == 'ㅆ') {
                    int[][] JongArray = MATCH_H2B_GRAMMAR.get(String.valueOf(hangul));
                    for (int j = 0; j < JongArray.length; j++) {
                        result.add(JongArray[j]);
                    }

                }

                // 그 외 종성에 해당하는 점자 MATCH_H2B_JONG(hashmap)애서 찾아서 result 리스트에 추가
                if (i == 2 && MATCH_H2B_JONG.containsKey(String.valueOf(hangul)) && !(hangul == 'ㅆ')) {
                    int[][] JongArray = MATCH_H2B_JONG.get(String.valueOf(hangul));
                    for (int j = 0; j < JongArray.length; j++) {
                        result.add(JongArray[j]);
                    }
                }
            }

            return (result);

        }
    }


    // 한글sentence를 한글자씩 쪼개서 ex) 안녕 = 안,녕
    // '안'과'녕' 하나하나씩만 letter함수 인자로 보냄
    public static ArrayList<Object> text(String HangulSentence) {
        ArrayList<Object> FinalResult = new ArrayList<Object>();
        for (char HangulLetter : HangulSentence.toCharArray()) {
            String HangulLetterStr = Character.toString(HangulLetter);
            ArrayList<Object> result = letter(HangulLetterStr);
            for (Object obj : result) {
                FinalResult.add(obj);
            }
        }
        return FinalResult;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void speakOut(String str) {
        tts.setPitch((float) 1.0);
        tts.setSpeechRate((float) 1.0);
        tts.speak(str, TextToSpeech.QUEUE_FLUSH,null,"id1");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS)  {
            int result = tts.setLanguage(Locale.KOREA);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {

            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }


    public void onClickButtonPaired(View view){
        btArrayAdapter.clear();
        if(deviceAddressArray!=null && !deviceAddressArray.isEmpty()){ deviceAddressArray.clear(); }
        pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                btArrayAdapter.add(deviceName);
                deviceAddressArray.add(deviceHardwareAddress);
            }
        }
    }

    public void onClickButtonSearch(View view){
        // Check if the device is already discovering
        if(btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();
        } else {
            if (btAdapter.isEnabled()) {
                btAdapter.startDiscovery();
                btArrayAdapter.clear();
                if (deviceAddressArray != null && !deviceAddressArray.isEmpty()) {
                    deviceAddressArray.clear();
                }
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(receiver, filter);
            } else {
                Toast.makeText(getApplicationContext(), "bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Send string "braille"
    public void onClickButtonSend(View view) {
        CharSequence CharSeq = txtText.getText();
        String sendtext = CharSeq.toString();
        Log.d("txttxt", sendtext);
        if(connectedThread!=null){ connectedThread.write(sendtext); }
    }

    // Send "a"
    public void onClickButtonSendA(View view) {
        if(connectedThread!=null){ connectedThread.write("a"); }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                btArrayAdapter.add(deviceName);
                deviceAddressArray.add(deviceHardwareAddress);
                btArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

    public class myOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(getApplicationContext(), btArrayAdapter.getItem(position), Toast.LENGTH_SHORT).show();

            textStatus.setText("try...");

            final String name = btArrayAdapter.getItem(position); // get name
            final String address = deviceAddressArray.get(position); // get address
            boolean flag = true;

            BluetoothDevice device = btAdapter.getRemoteDevice(address);

            // create & connect socket
            try {
                btSocket = createBluetoothSocket(device);
                btSocket.connect();
            } catch (IOException e) {
                flag = false;
                textStatus.setText("connection failed!");
                e.printStackTrace();
            }

            // start bluetooth communication
            if(flag){
                textStatus.setText("connected to "+name);
                connectedThread = new ConnectedThread(btSocket);
                connectedThread.start();
            }

        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }
}