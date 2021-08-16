package app.mobilebrainz.fastpizza.admin.viewmodel;

import androidx.lifecycle.ViewModel;

/**
 * ViewModel главной активность MainActivity для сохранения глобальног состояния при конфигурированнии
 * приложения (повороте или сворачивании экрана).
 */
public class MainViewModel extends ViewModel {

    /**
     * Вошёл ли пользователь
     */
    private boolean isSigningIn;

    public MainViewModel() {
        isSigningIn = false;
    }

    public boolean getIsSigningIn() {
        return isSigningIn;
    }

    public void setIsSigningIn(boolean mIsSigningIn) {
        this.isSigningIn = mIsSigningIn;
    }

}
