/*
 * Decompiled with CFR 0.152.
 */
package de.audi.app.terminalmode.smartphone.androidauto2.nav;

import de.audi.app.terminalmode.IContext;
import de.audi.app.terminalmode.smartphone.androidauto2.AbstractAndroidAuto2Handler;
import de.audi.app.terminalmode.smartphone.androidauto2.nav.IAndroidAuto2NavHandler;
import de.audi.app.terminalmode.statemachine.Application;
import de.audi.app.terminalmode.statemachine.ApplicationOwner;
import de.audi.app.terminalmode.statemachine.IStateHandler;
import de.audi.atip.log.LogChannel;
import org.dsi.ifc.androidauto2.DSIAndroidAuto2;

public class AndroidAuto2NavHandler
extends AbstractAndroidAuto2Handler
implements IAndroidAuto2NavHandler {
    private static /*final*/ String LOGCLASS;
    private volatile ApplicationOwner appNaviOwnerCurrent = ApplicationOwner.NOBODY;
    private volatile boolean hasNavFocusRequested;
    private volatile ApplicationOwner expectedAppNavOwner = ApplicationOwner.NOBODY;

    public AndroidAuto2NavHandler(LogChannel logChannel, DSIAndroidAuto2 dSIAndroidAuto2, IStateHandler iStateHandler, IContext iContext) {
        super(logChannel, dSIAndroidAuto2, iStateHandler, iContext);
    }

   // @Override
    public void updateNavFocus(ApplicationOwner applicationOwner) {
        //System.out.println("AA AndroidAuto2NavHandler.updateNavFocus() 1");
        if (this.hasNavFocusRequested) {
            //System.out.println("AA hasNavFocusRequested 2");
            if (this.expectedAppNavOwner.equals(applicationOwner)) {
                //System.out.println("AA expectedAppNavOwner.equals(applicationOwner) 3");
                this.logger.log(1078071040, "[%1.updateNavFocus] responding to requested navFocusNotification: %2", (Object)"AndroidAuto2NavHandler", (Object)(ApplicationOwner.DEVICE.equals(applicationOwner) ? "PROJECTED" : "NATIVE"));
                this.dsi.navFocusNotification(ApplicationOwner.DEVICE.equals(applicationOwner) ? 2 : 1, false); //(2, false) allows dual navigation activating, deactivating and reactivating maps
                this.hasNavFocusRequested = false;
                this.expectedAppNavOwner = ApplicationOwner.NOBODY;
            }
        } else {
            //System.out.println("AA NOT hasNavFocusRequested 4");
            if (this.appNaviOwnerCurrent.equals(applicationOwner)) {
                //System.out.println("AA appNaviOwnerCurrent.equals(applicationOwner) 5");
                this.logger.log(1078071040, "[%1.updateNavFocus] %2 -> %3, do nothing", (Object)"AndroidAuto2NavHandler", (Object)this.appNaviOwnerCurrent, (Object)applicationOwner);
                return;
            }
            if (ApplicationOwner.DEVICE.equals(applicationOwner)) {
                //System.out.println("AA ApplicationOwner equals DEVICE 6");
                this.logger.log(1078071040, "[%1.updateNavFocus] %2 -> %3, navFocusNotification: PROJECTED", (Object)"AndroidAuto2NavHandler", (Object)this.appNaviOwnerCurrent, (Object)applicationOwner);
                this.dsi.navFocusNotification(2, true);
            } else {
                //System.out.println("AA ApplicationOwner NOT equals DEVICE 7");
                this.logger.log(1078071040, "[%1.updateNavFocus] %2 -> %3, navFocusNotification: NATIVE", (Object)"AndroidAuto2NavHandler", (Object)this.appNaviOwnerCurrent, (Object)applicationOwner);
                //this.dsi.navFocusNotification(1, true); //commented to stop native from deactivating maps
            }
        }
        this.appNaviOwnerCurrent = applicationOwner;
    }

   // @Override
    public void navFocusRequestNotification(int n, int n2) {
        //System.out.println("AA AndroidAuto2NavHandler.navFocusRequestNotification() 8");
        if (this.isValid(n2)) {
            this.logger.log(1078071040, "<- [%1.navFocusRequestNotification] %2", (Object)"AndroidAuto2NavHandler", (Object)this.getNavState(n));
            if (1 == n) {
                //System.out.println("AA n==1 9");
                this.requestDSIUpdate(Application.NAVI, ApplicationOwner.NOBODY);
                this.hasNavFocusRequested = true;
                this.expectedAppNavOwner = ApplicationOwner.NOBODY;
            } else if (2 == n) {
                //System.out.println("AA n==2 10");
                //this.requestDSIUpdate(Application.NAVI, ApplicationOwner.DEVICE); //commented to stop maps from deactivating native
                this.hasNavFocusRequested = true;
                this.expectedAppNavOwner = ApplicationOwner.DEVICE;
                this.updateNavFocus(ApplicationOwner.DEVICE); //added to activate device navigation anyway
            }
        }
    }

    private String getNavState(int n) {
        switch (n) {
            case 1: {
                return "NAV_NATIVE";
            }
            case 2: {
                return "NAV_PROJECTED";
            }
        }
        return new StringBuffer().append("NAV_UNKNOWN ").append(n).toString();
    }

   // @Override
    protected String getLogClass() {
        return "AndroidAuto2NavHandler";
    }
}

