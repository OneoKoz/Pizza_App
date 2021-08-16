package app.mobilebrainz.fastpizza.fragment;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.LayoutRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import app.mobilebrainz.fastpizza.R;

/**
 * Базовый фрагмент
 */
public class BaseFragment extends Fragment {

    private Snackbar errorSnackbar;
    private Snackbar infoSnackbar;
    private CoordinatorLayout coordinatorLayout;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        coordinatorLayout = getActivity().findViewById(R.id.coordinator_layout);
    }

    protected View inflate(@LayoutRes int layoutRes, @Nullable ViewGroup container) {
        return LayoutInflater.from(getContext()).inflate(layoutRes, container, false);
    }

    protected void navigate(@NonNull NavDirections directions) {
        if (getView() != null) {
            Navigation.findNavController(getView()).navigate(directions);
        }
    }

    @MainThread
    protected void showInfoSnackbar(@StringRes int resId) {
        infoSnackbar = Snackbar.make(coordinatorLayout, resId, Snackbar.LENGTH_LONG);
        showSnackbar(infoSnackbar);
    }

    @MainThread
    protected void showErrorSnackbar(@StringRes int messageResId, @StringRes int actionResId, View.OnClickListener action) {
        errorSnackbar = Snackbar.make(coordinatorLayout, messageResId, Snackbar.LENGTH_INDEFINITE);
        errorSnackbar.setAction(actionResId, action);
        showSnackbar(errorSnackbar);
    }

    private void showSnackbar(Snackbar snackbar) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackbar.getView().getLayoutParams();
        params.setAnchorId(R.id.nav_view);
        params.anchorGravity = Gravity.TOP;
        params.gravity = Gravity.TOP;
        snackbar.getView().setLayoutParams(params);
        snackbar.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (errorSnackbar != null && errorSnackbar.isShown()) {
            errorSnackbar.dismiss();
        }
        if (infoSnackbar != null && infoSnackbar.isShown()) {
            infoSnackbar.dismiss();
        }
    }

}
