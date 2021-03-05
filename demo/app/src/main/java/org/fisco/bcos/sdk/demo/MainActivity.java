package org.fisco.bcos.sdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView tvChainInfo;
    private EditText etChainId;
    private EditText etGroupId;
    private RadioButton rbECDSA;
    private RadioButton rbGM;
    private EditText etIPPort;
    private TextView tvKeyInfo;
    private RadioButton rbRadonKey;
    private RadioButton rbSpecifyKey;
    private EditText etSpecifyKey;
    private TextView tvContractInfo;
    private RadioButton rbWrapper;
    private RadioButton rbAbiBin;
    private Button btnDeployCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initEvent();
    }

    private void initView() {
        // chain info
        tvChainInfo = findViewById(R.id.tv_chain_info_id);
        TextPaint tp1 = tvChainInfo.getPaint();
        tp1.setFakeBoldText(true);
        etChainId = findViewById(R.id.et_chain_id);
        etGroupId = findViewById(R.id.et_group_id);
        rbGM = findViewById(R.id.rb_GM);
        rbECDSA = findViewById(R.id.rb_ECDSA);
        etIPPort = findViewById(R.id.et_ip_port);

        // key info
        tvKeyInfo = findViewById(R.id.tv_key_info_id);
        TextPaint tp2 = tvKeyInfo.getPaint();
        tp2.setFakeBoldText(true);
        rbRadonKey = findViewById(R.id.rb_key_radom);
        rbSpecifyKey = findViewById(R.id.rb_key_specify);
        etSpecifyKey = findViewById(R.id.et_key_specify);

        // contract used way
        tvContractInfo = findViewById(R.id.tv_contract_info_id);
        TextPaint tp3 = tvContractInfo.getPaint();
        tp3.setFakeBoldText(true);
        rbWrapper = findViewById(R.id.rb_wrapper);
        rbAbiBin = findViewById(R.id.rb_abi_bin);
        btnDeployCall = findViewById(R.id.btn_deploy_call);
    }

    private void initEvent() {
        btnDeployCall.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, DeployLoadActivity.class);
                        intent.putExtra("chainId", etChainId.getText().toString().trim());
                        intent.putExtra("groupId", etGroupId.getText().toString().trim());
                        intent.putExtra("transactType", rbECDSA.isChecked());
                        intent.putExtra("ipPort", etIPPort.getText().toString().trim());
                        intent.putExtra("keyType", rbRadonKey.isChecked());
                        intent.putExtra("keyContent", etSpecifyKey.getText().toString().trim());
                        intent.putExtra("transactInfo", rbWrapper.isChecked());
                        startActivity(intent);
                    }
                });
    }
}
