package com.reactnativenavigation.viewcontrollers;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.reactnativenavigation.parse.Options;
import com.reactnativenavigation.parse.params.Bool;
import com.reactnativenavigation.presentation.Presenter;
import com.reactnativenavigation.utils.CollectionUtils;
import com.reactnativenavigation.views.Component;

import java.util.Collection;

import androidx.annotation.CallSuper;
import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import static com.reactnativenavigation.utils.CollectionUtils.*;
import static com.reactnativenavigation.utils.ObjectUtils.perform;

public abstract class ParentController<T extends ViewGroup> extends ChildController {

	public ParentController(Activity activity, ChildControllersRegistry childRegistry, String id, Presenter presenter, Options initialOptions) {
		super(activity, childRegistry, id, presenter, initialOptions);
	}

    @Override
    public void setWaitForRender(Bool waitForRender) {
        super.setWaitForRender(waitForRender);
        applyOnController(getCurrentChild(), controller -> ((ViewController) controller).setWaitForRender(waitForRender));
    }

    @Override
    public void setDefaultOptions(Options defaultOptions) {
	    super.setDefaultOptions(defaultOptions);
	    forEach(getChildControllers(), (child) -> child.setDefaultOptions(defaultOptions));
    }

    @Override
    @CheckResult
    public Options resolveCurrentOptions() {
	    if (CollectionUtils.isNullOrEmpty(getChildControllers())) return initialOptions;
        return getCurrentChild()
                .resolveCurrentOptions()
                .copy()
                .withDefaultOptions(initialOptions);
    }

    public Options resolveChildOptions(ViewController child) {
	    if (child == this) return resolveCurrentOptions();
        return child
                .resolveCurrentOptions()
                .copy()
                .withDefaultOptions(initialOptions);
    }

    @Override
    @CheckResult
    public Options resolveCurrentOptions(Options defaultOptions) {
        return resolveCurrentOptions().withDefaultOptions(defaultOptions);
    }

    public boolean isCurrentChild(ViewController child) {
        return getCurrentChild() == child;
    }

    protected abstract ViewController getCurrentChild();

    @NonNull
	@Override
	public T getView() {
		return (T) super.getView();
	}

	@NonNull
	@Override
	protected abstract T createView();

    @NonNull
	public abstract Collection<? extends ViewController> getChildControllers();

	@Nullable
	@Override
	public ViewController findController(final String id) {
		ViewController fromSuper = super.findController(id);
		if (fromSuper != null) return fromSuper;

		for (ViewController child : getChildControllers()) {
			ViewController fromChild = child.findController(id);
			if (fromChild != null) return fromChild;
		}

		return null;
	}

    @Nullable
    @Override
    public ViewController findController(View child) {
        ViewController fromSuper = super.findController(child);
        if (fromSuper != null) return fromSuper;

        for (ViewController childController : getChildControllers()) {
            ViewController fromChild = childController.findController(child);
            if (fromChild != null) return fromChild;
        }

        return null;
    }

    @Override
    public boolean containsComponent(Component component) {
        if (super.containsComponent(component)) {
            return true;
        }
        for (ViewController child : getChildControllers()) {
            if (child.containsComponent(component)) return true;
        }
        return false;
    }

    @CallSuper
    public void applyChildOptions(Options options, ViewController child) {
        this.options = initialOptions.mergeWith(options);
    }

    @CallSuper
    public void mergeChildOptions(Options options, ViewController childController) {

    }

	@Override
	public void destroy() {
		super.destroy();
		forEach(getChildControllers(), ViewController::destroy);
	}

	@SuppressWarnings("WeakerAccess")
    @CallSuper
    protected void clearOptions() {
	    performOnParentController(parent -> ((ParentController) parent).clearOptions());
        options = initialOptions.copy().clearOneTimeOptions();
    }

    public void setupTopTabsWithViewPager(ViewPager viewPager) {

    }

    public void clearTopTabs() {

    }

    @Override
    public boolean isRendered() {
        return getCurrentChild() != null && getCurrentChild().isRendered();
    }

    public void onChildDestroyed(ViewController child) {

    }

    @Override
    public void applyTopInset() {
	    forEach(getChildControllers(), ViewController::applyTopInset);
    }

    public int getTopInset(ViewController child) {
        return perform(getParentController(), 0, p -> p.getTopInset(child));
    }

    @Override
    public void applyBottomInset() {
        forEach(getChildControllers(), ViewController::applyBottomInset);
    }

    public int getBottomInset(ViewController child) {
        return perform(getParentController(), 0, p -> p.getBottomInset(child));
    }
}
