package app.mobilebrainz.fastpizza.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import app.mobilebrainz.fastpizza.R;

import static app.mobilebrainz.fastpizza.App.CLIENT;
import static app.mobilebrainz.fastpizza.App.EMAIL;

/**
 * Фрагмент информации компани с конактными данными и кнопка создания почтового сообщения.
 */
public class ContactsFragment extends BaseFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflate(R.layout.fragment_contacts, container);

        TextView infoView = view.findViewById(R.id.infoView);
        infoView.setText(Html.fromHtml(getString(R.string.info_view)));

        Button emailBtn = view.findViewById(R.id.emailBtn);
        emailBtn.setText(EMAIL);
        emailBtn.setOnClickListener(v -> sendMail());

        return view;
    }

    private void sendMail() {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{EMAIL});
            intent.putExtra(Intent.EXTRA_SUBJECT, CLIENT);
            startActivity(Intent.createChooser(intent, getString(R.string.choose_email_client)));
        } catch (android.content.ActivityNotFoundException ex) {
            showInfoSnackbar(R.string.send_mail_error);
        }
    }
}