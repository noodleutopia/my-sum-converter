package com.example.zyf.sumconverter;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    static final String UP_OVERFLOW = "超过最大金额数，请重新输入！";
    static final String INPUT_ERROR = "输入有误，请重新输入！";
    static final String[] units = {"分", "角", "元", "万", "亿"};
    static final String[] decimalSys = {"", "拾", "佰", "仟"};
    static final String[] upperDigits = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    static final int MAX_INT_LENGTH = 16;   //整数最大位数
    EditText inputEt;
    TextView resultTv;
    Button okBtn;
    Button clearBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    /**
     * 转换算法
     * 算法思路：
     * 1.将输入的字符串以小数点为界，分为整数、小数两部分分别处理；
     * 2.将整数部分由低位向高位以 4 位一组划分为字符串数组；
     * 3.以"元","万","亿"，“万亿”为界，分别处理每一段数字；
     * 4.将数组间连续多个“零”变成一个，并去掉“零元”；
     * 5.处理小数部分
     *
     * @param input 输入的数字
     * @return 大写金额
     */
    private String convert(String input) {

        if (input.endsWith(".")) {
            return INPUT_ERROR;
        }
        StringBuilder sb = new StringBuilder();
        int currentDigit = 0;
        String partInt = null;
        String partDec = null;
        if (!input.contains(".")) { //若输入不含小数
            partInt = input;
        } else {    //若输入含小数
            String[] array = input.split("\\.");
            if (!TextUtils.isEmpty(array[0])) {
                partInt = array[0];
            }
            partDec = array[1];
        }

        //整数部分
        if (partInt != null) {
            //将开头的0去掉
            int mark = 0;
            while (mark < partInt.length() - 1 && partInt.charAt(mark) - '0' == 0) {
                mark++;
            }
            partInt = partInt.substring(mark, partInt.length());
            int length = partInt.length();
            if (length > MAX_INT_LENGTH) {  //整数超过16位则超出范围
                return UP_OVERFLOW;
            }
            long res = Long.parseLong(partInt);
            if (res > 0) {
                //将整数部分拆分成4位一组的字符串数组
                int vol = 1 + (partInt.length() - 1) / 4;
                String[] strArray = new String[vol];
                int temp = ((length % 4) == 0) ? 4 : (length % 4);
                strArray[0] = partInt.substring(0, temp);
                for (int i = 1; i < vol; i++) {
                    strArray[i] = partInt.substring(temp + (i - 1) * 4, temp + i * 4);
                }
                //以"元"，"万"，"亿"，“万亿”为界，分别处理每一段数字；
                int j = 0;
                if (vol > 3) {  //千万亿以内，千亿以上部分
                    for (j = 0; j < vol - 3; j++) {
                        String digits = formatDigits(strArray[j]);
                        sb.append(digits);
                        if (!digits.equals(upperDigits[0])) {
                            sb.append(units[vol - 1 - j]);
                        }
                    }
                    if (Integer.parseInt(strArray[1]) == 0) {   //如果亿到千亿位都是0，加上“亿”
                        sb.append(units[4]);
                    }
                }

                for (int i = j; i < vol; i++) { //千亿以内的部分
                    String digits = formatDigits(strArray[i]);
                    sb.append(digits);
                    if (i < vol - 1) {  //大于千的部分
                        if (!digits.equals(upperDigits[0])) {
                            sb.append(units[1 + vol - i]);
                        }
                    } else { //小于千的部分
                        sb.append(units[2]); //最终总要加“元”
                    }
                }

                //处理连续多个“零”和“零元”
                int index = sb.indexOf("零零");
                while (index != -1) {
                    sb.deleteCharAt(index);
                    index = sb.indexOf("零零");
                }
                index = sb.indexOf("零元");
                if (index != -1) {
                    sb.deleteCharAt(index);
                }
            }
        }

        //小数部分
        if (partDec == null || Integer.parseInt(partDec) == 0) {    //无小数
            if (partInt == null || Long.parseLong(partInt) == 0l) {
                sb.append(upperDigits[0]).append(units[2]); //整数小数皆为0，零元整
            }
            sb.append("整");
        } else {
            for (int i = 0; i < partDec.length(); i++) {
                currentDigit = partDec.charAt(i) - '0';
                if (currentDigit == 0) {
                    if (i == 0 && sb.length() > 0) {    //角位且整数部分不为0
                        sb.append(upperDigits[0]);
                    }
                } else {
                    sb.append(upperDigits[currentDigit]).append(units[1 - i]);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 将每段数字转换为大写数字（此时每段数字长度不超过4）
     *
     * @param in 输入的数字
     * @return 返回的大写数字
     */
    private String formatDigits(String in) {
        if (Integer.parseInt(in) <= 0) {    //如果全为0，返回“零”
            return upperDigits[0];
        }
        StringBuilder sb = new StringBuilder();
        int length = in.length();
        for (int i = 0; i < length; i++) {
            int currentDigit = in.charAt(i) - '0';
            if (currentDigit != 0) { //数字不为0，返回数字和单位
                sb.append(upperDigits[currentDigit]).append(decimalSys[length - 1 - i]);
            } else if (i < length - 1) { //数字为0，且不为最后一位，则只添加最后一个“零”
                if (in.charAt(i + 1) - '0' != 0) {
                    sb.append(upperDigits[0]);
                }
            }
        }
        return sb.toString();
    }

    private void initView() {
        inputEt = (EditText) findViewById(R.id.input_et);
        resultTv = (TextView) findViewById(R.id.result_tv);
        okBtn = (Button) findViewById(R.id.ok_button);
        clearBtn = (Button) findViewById(R.id.clear_button);
        okBtn.setOnClickListener(this);
        clearBtn.setOnClickListener(this);
        inputEt.addTextChangedListener(new TextWatcher() {  //控制小数点后只能输入两位
            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(resultTv.getText())) {
                    resultTv.setText("");
                }
                String temp = editable.toString();
                int posDot = temp.indexOf(".");
                if (posDot < 0) return;
                if (temp.length() - posDot - 1 > 2) {
                    editable.delete(posDot + 3, temp.length());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ok_button:
                String res = convert(inputEt.getText().toString());
                if (!TextUtils.isEmpty(res)) {
                    resultTv.setText(res);
                }
                break;
            case R.id.clear_button:
                inputEt.setText("");
                resultTv.setText("");
                break;
        }
    }
}
