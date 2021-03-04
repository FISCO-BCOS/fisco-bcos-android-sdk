package org.fisco.bcos.sdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivityA extends AppCompatActivity {
    private EditText etChainId;
    private EditText etGroupId;
    private RadioButton rbGM;
    private RadioButton rbECDSA;
    private RadioButton rbRadonKey;
    private RadioButton rbSpecifyKey;
    private EditText etSpecifyKey;
    private EditText etIPPort;
    private Button btnDeployLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initEvent();

    }

    private void initView() {
        etChainId = findViewById(R.id.et_chain_id);
        etGroupId = findViewById(R.id.et_group_id);
        rbGM = findViewById(R.id.rb_GM);
        rbECDSA = findViewById(R.id.rb_ECDSA_TYPE);
        rbRadonKey = findViewById(R.id.rb_key_radom);
        rbSpecifyKey = findViewById(R.id.rb_key_specify);
        etSpecifyKey = findViewById(R.id.et_key_specify);
        etIPPort = findViewById(R.id.et_ip_port);
        btnDeployLoad = findViewById(R.id.btn_deploy_load);
    }

    private void initEvent() {
        btnDeployLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivityA.this, DeployLoadActivity.class);
                intent.putExtra("chainId", etChainId.getText().toString().trim());
                intent.putExtra("groupId", etGroupId.getText().toString().trim());
                intent.putExtra("transactType", rbECDSA.isChecked());
                intent.putExtra("keyType", rbRadonKey.isChecked());
                intent.putExtra("ipPort", etIPPort.getText().toString().trim());
                startActivity(intent);
            }
        });
    }


}
