package global;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.Calendar;
import java.util.Date;

public class Planary extends MIDlet implements CommandListener {

    static final int MoneyPos = 0;
    static final int TodayPos = 1;
    static final int WeekPos = 2;
    static final int HaosPos = 3;
    static final int TasksPos = 4;
    static final int ArchivePos = 5;
    
    static final int MoneyFormAction_Add = 0;
    static final int MoneyFormAction_Edit = 1;
    static final int MoneyFormAction_Delete = 2;
    static final int MoneyFormAction_Balance = 3;
    
    private List frmMain;
    private Display disp;
    private Command cmdSelect,  cmdExit,  cmdBack,  cmdNew,  cmdOK,  cmdCancel,
            cmdDelete,  cmdMove1st,  cmdMove2nd,  cmdDoneTask,  cmdClear,
            cmdEdit,  cmdNext;
    private int pos;
    private Form frmArchive;
    private int intTasksID[];
    private String strList[];
    private boolean SelectedArray[];
    private int SelectedTaskID[];
    private RecordStoreHelper PSH;
    private List frmTaskList;
    private List frmMoney_Menu,frmMoneyDirection;
    private TextBox frmNewTask,frmMoneyGetText;
    private boolean EditFormState;
    private int MoneyFormAction;
    private int MoneyAddEditStep;
    private boolean isRashod;
    private int Summa;
    private String TextOperation;

    String[] strLargeMonths = {"������", "�������", "�����", "������", "���", "����",
        "����", "�������", "��������", "�������", "������", "�������"
    };
    String[] strMonths = {"���", "���", "����", "���", "���", "���",
        "���", "���", "����", "���", "����", "���"
    };
    String[] strLargeDayOfWeek = {"�����������", "�����������", "�������", "�����",
        "�������", "�������", "�������"
    };
    String[] strDayOfWeek = {"����", "���", "����", "���", "���", "���", "���"};

    public Planary() {
        PSH = new RecordStoreHelper();

        PSH.Update();

        Calendar clCurrent = Calendar.getInstance();
        clCurrent.setTime(new Date());

        int m = clCurrent.get(Calendar.MONTH);
        int d = clCurrent.get(Calendar.DAY_OF_MONTH);
        int y = clCurrent.get(Calendar.YEAR);
        int w = clCurrent.get(Calendar.DAY_OF_WEEK) - 1;

        String strCurDate = new String(Integer.toString(d) + " " +
                strMonths[m] + " " + Integer.toString(y) + " �. (" + strDayOfWeek[w] + ")");
        String[] strMenuList = {"��� ������", "�������", "������", "����", "��� ����", "����� �������"};
        String[] MoneyMenuList = {"��������", "��������", "�������", "������"};

        try {
            Image imgList[] = {Image.createImage("/fiol.png"), Image.createImage("/red.png"),
                Image.createImage("/orange.png"), Image.createImage("/yellow.png"), Image.createImage("/light.png"),
                Image.createImage("/white.png")
            };
            frmMain = new List("", List.IMPLICIT, strMenuList, imgList);
        } catch (Exception e) {
            destroyApp(true);
        }

        Font big_style = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
        
        
        for (int i = 0; i < frmMain.size(); i++) {
            frmMain.setFont(i, big_style);
        }
        

        frmMain.setTitle(strCurDate);

        cmdSelect = new Command("�����", Command.ITEM, 0);
        frmMain.addCommand(cmdSelect);

        cmdExit = new Command("�����", Command.EXIT, 0);
        frmMain.addCommand(cmdExit);

        cmdBack = new Command("�����", Command.BACK,1); //����� ��� ���� ������� Back, ���������� � ������� ����
        cmdNext=new Command("�����",Command.ITEM,0);
        cmdOK = new Command("OK", Command.OK, 1);
        cmdCancel = new Command("�����", Command.CANCEL, 1);

        String dirList[]={"������","�����"};
        frmMoneyDirection=new List("�����������",List.EXCLUSIVE,dirList,null);
        frmMoneyDirection.addCommand(cmdNext);
        frmMoneyDirection.addCommand(cmdBack);
        frmMoneyDirection.setCommandListener(this);

        frmMoney_Menu = new List("",List.IMPLICIT,MoneyMenuList,null);
        frmMoney_Menu.setTitle("��������");
        for (int i = 0; i < frmMoney_Menu.size(); i++) {
            frmMoney_Menu.setFont(i, big_style);
        }
        frmMoney_Menu.addCommand(cmdSelect);
        frmMoney_Menu.addCommand(cmdCancel);

        frmMoney_Menu.setCommandListener(this);
        frmMain.setCommandListener(this);
    }

    private void ShowBalance(){
        int dohod,rashod,balance,temp;
        dohod=rashod=balance=temp=0;
        Form frmBalance=new Form("������");
        int count = PSH.GetCount(MoneyPos);
        if (count > 0) {
            strList = new String[count];
            intTasksID = new int[count];

            PSH.LoadRecords(strList, intTasksID, MoneyPos);
            for(int i=0;i<strList.length;i++)
            {
                try{
                    temp=Integer.parseInt(strList[i].substring(strList[i].indexOf("|")+1));
                }
                catch(java.lang.NumberFormatException e) {;}
                finally
                {
                    if (temp<0) rashod+=Math.abs(temp);
                    else dohod+=temp;
                }
            }
            balance=dohod-rashod;
        }
        else
        {
            dohod=rashod=balance=0;
        }

        Font bold_styled = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);

        StringItem si1 = new StringItem("","������: ", StringItem.PLAIN);
        si1.setFont(bold_styled);
        frmBalance.append(si1);
        frmBalance.append(String.valueOf(balance));
        frmBalance.append("���.\n");

        StringItem si2 = new StringItem("", "������: ", StringItem.PLAIN);
        si2.setFont(bold_styled);
        frmBalance.append(si2);
        frmBalance.append(String.valueOf(dohod));
        frmBalance.append("���.\n");

        StringItem si3 = new StringItem("", "�������: ", StringItem.PLAIN);
        si3.setFont(bold_styled);
        frmBalance.append(si3);
        frmBalance.append(String.valueOf(rashod));
        frmBalance.append("���.\n");

        frmBalance.addCommand(cmdBack);
        frmBalance.setCommandListener(this);
        disp.setCurrent(frmBalance);
    }

    private void ShowArchive() {
        frmArchive = new Form("����� �������");

        int count = PSH.GetCount(ArchivePos);
        if (count > 0) {
            String strArcList[] = new String[count];
            Long Dates[] = new Long[count];

            PSH.LoadArchiveRecords(strArcList, Dates);

            Calendar clOne = Calendar.getInstance();
            int m1, d1, y1, m2, d2, y2; //two dates' info
            int i, j; //circle vars
            Font bold_styled = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);

            for (i = 0; i < strArcList.length;) {
                clOne.setTime(new Date(Dates[i].longValue()));

                m1 = clOne.get(Calendar.MONTH);
                d1 = clOne.get(Calendar.DAY_OF_MONTH);
                y1 = clOne.get(Calendar.YEAR);

                String strDate = new String(String.valueOf(d1) + " " + strMonths[m1] + " " + String.valueOf(y1) + " �. :");
                StringItem siOne = new StringItem("", strDate, StringItem.PLAIN);
                //siOne.setLayout(StringItem.LAYOUT_CENTER);
                siOne.setFont(bold_styled);
                frmArchive.append(siOne);

                String OutStr = "";

                for (j = i; j < strArcList.length; j++) {
                    clOne.setTime(new Date(Dates[j].longValue()));
                    m2 = clOne.get(Calendar.MONTH);
                    d2 = clOne.get(Calendar.DAY_OF_MONTH);
                    y2 = clOne.get(Calendar.YEAR);

                    if ((m1 != m2) || (d1 != d2) || (y1 != y2)) {
                        break;
                    }

                    OutStr += "\n- " + strArcList[j];
                }

                i = j;

                StringItem siInfo = new StringItem("", OutStr, StringItem.PLAIN);
                //siInfo.setLayout(StringItem.LAYOUT_LEFT);
                frmArchive.append(siInfo);

                frmArchive.append("\n");

            }

            cmdClear = new Command("��������", Command.SCREEN, 0);
            frmArchive.addCommand(cmdClear);
        } else {
            frmArchive.append("[������� ���� ���]");
        }
        frmArchive.addCommand(cmdBack);
        frmArchive.setCommandListener(this);
        disp.setCurrent(frmArchive);
    }

    private void BackAfterAction() {
            switch (pos) {
                case MoneyPos:
                    disp.setCurrent(frmMain);
                    break;
                default:
                    if (PSH.GetCount(pos) > 0)
                        ShowListForm();
                    else disp.setCurrent(frmMain);
                    break;
            }
    }

    private int GetFirstTrueIndex(boolean BoolArray[]) {
        for (int i = 0; i < BoolArray.length; i++) {
            if (BoolArray[i] == true) {
                return i;
            }
        }
        return -1;
    }
    
    private void ShowMoneyAddEditForm(){
        switch(MoneyAddEditStep)
        {
            case 0:
                if (isRashod)
                    frmMoneyDirection.setSelectedIndex(0, true);
                else
                    frmMoneyDirection.setSelectedIndex(1, true);
                disp.setCurrent(frmMoneyDirection);
                break;
            case 1:
                isRashod=(frmMoneyDirection.getSelectedIndex()==0);
                frmMoneyGetText=new TextBox("�����", (MoneyFormAction==MoneyFormAction_Add)?"":String.valueOf(Summa), 10, TextField.DECIMAL);
                frmMoneyGetText.addCommand(cmdNext);
                frmMoneyGetText.addCommand(cmdBack);
                frmMoneyGetText.setCommandListener(this);
                disp.setCurrent(frmMoneyGetText);
                break;
            case 2:
                try
                {
                    Summa=Integer.parseInt(frmMoneyGetText.getString());
                }
                catch(java.lang.NumberFormatException e) {Summa=0;}
                frmMoneyGetText=new TextBox("����������", TextOperation, 100, TextField.ANY);
                frmMoneyGetText.addCommand(cmdOK);
                frmMoneyGetText.addCommand(cmdBack);
                frmMoneyGetText.setCommandListener(this);
                disp.setCurrent(frmMoneyGetText);
                break;
        }
    }
    
    public void commandAction(Command c, Displayable s) {
        if (c == cmdExit) {
            destroyApp(true);
            return;
        }

        if (c == Alert.DISMISS_COMMAND) {
            ShowListForm();
            return;
        }

        if ((c == cmdSelect) || (c == List.SELECT_COMMAND)) {

            if (s==frmMain)
            {
                pos = frmMain.getSelectedIndex();

                switch (pos) {
                    case MoneyPos:
                        ShowMoneyForm();
                        break;
                    case TodayPos:
                    case WeekPos:
                    case HaosPos:
                    case TasksPos:
                        ShowListForm();
                        break;
                    case ArchivePos:
                        ShowArchive();
                        break;
                }
                return;
            }

            if (s==frmMoney_Menu)
            {
                switch (frmMoney_Menu.getSelectedIndex()) {
                    case 0:
                        MoneyAddEditStep=0;
                        MoneyFormAction=MoneyFormAction_Add;
                        Summa=0; isRashod=true; TextOperation="";
                        ShowMoneyAddEditForm();
                        break;
                    case 1:
                        MoneyFormAction=MoneyFormAction_Edit;
                        ShowListForm();
                        break;
                    case 2:
                        MoneyFormAction=MoneyFormAction_Delete;
                        ShowListForm();
                        break;
                    case 3:
                        MoneyFormAction=MoneyFormAction_Balance;
                        ShowBalance();
                        break;
                }
                return;
            }
            if (s==frmMoneyDirection)
            {
                MoneyAddEditStep++;
                ShowMoneyAddEditForm();
                return;
            }
        }

        if (c == cmdNext) {
            MoneyAddEditStep++;
            ShowMoneyAddEditForm();
            return;
        }

        //Archive.Commands

        if (c == cmdClear) {
            PSH.ClearArchiveRecords();
            disp.setCurrent(frmMain);
            return;
        }
            
        //PlanList.Commands

        if ((c == cmdDelete) || (c == cmdMove1st) || (c == cmdMove2nd) || (c == cmdDoneTask) || (c == cmdEdit)) {
            SelectedArray = new boolean[intTasksID.length];
            int selcount=0;
            
            if (s==frmTaskList)
                selcount=frmTaskList.getSelectedFlags(SelectedArray);
            int i = 0, j = 0;

            if (selcount == 0) {
                return;
            }

            SelectedTaskID = new int[selcount];
            for (i = 0; i < SelectedArray.length; i++) {
                if (SelectedArray[i] == true) {
                    SelectedTaskID[j++] = intTasksID[i];
                }
            }

        }

        if (c == cmdBack) {
            if (pos==MoneyPos)
            {
                if (MoneyAddEditStep>0)
                {
                    MoneyAddEditStep--;
                    ShowMoneyAddEditForm();
                }
                else disp.setCurrent(frmMoney_Menu);
            }
            else disp.setCurrent(frmMain);
            return;
        }
        
        if (c == cmdNew) {
            if (pos!=MoneyPos)
                ShowNewTaskForm();
            else
            {
                MoneyAddEditStep=0;
                MoneyFormAction=MoneyFormAction_Add;
                Summa=0; isRashod=true; TextOperation="";
                ShowMoneyAddEditForm();
            }
        }
        
        if (c == cmdEdit) {
            if (pos!=MoneyPos)
            {
                EditFormState = true;
                ShowEditForm("��������� ������", frmTaskList.getString(GetFirstTrueIndex(SelectedArray)));
            }
            else
            {
                MoneyAddEditStep=0;
                MoneyFormAction=MoneyFormAction_Edit;
                String RawText=PSH.LoadRecordByID(SelectedTaskID[0]);
                try
                {
                    Summa=Integer.parseInt(RawText.substring(RawText.indexOf("|")+1));
                }
                catch(java.lang.NumberFormatException e) {Summa=0;}
                isRashod=(Summa<0); TextOperation=RawText.substring(0,RawText.indexOf("|"));
                Summa=Math.abs(Summa);
                ShowMoneyAddEditForm();
            }   
            return;
        }
        if (c == cmdDelete) {
            PSH.Delete(SelectedTaskID);
            if (pos==MoneyPos) disp.setCurrent(frmMoney_Menu);
            else BackAfterAction();
            return;
        }
        if (c == cmdMove1st) {
            if (pos == TodayPos) {
                PSH.MoveTo(SelectedTaskID, WeekPos);
            }
            if (pos == WeekPos) {
                PSH.MoveTo(SelectedTaskID, TodayPos);
            }
            if (pos == HaosPos) {
                PSH.MoveTo(SelectedTaskID, TodayPos);
            }
            BackAfterAction();
            return;
        }
        
        if (c == cmdMove2nd) {
            if (pos == TodayPos) {
                PSH.MoveTo(SelectedTaskID, HaosPos);
            }
            if (pos == WeekPos) {
                PSH.MoveTo(SelectedTaskID, HaosPos);
            }
            if (pos == HaosPos) {
                PSH.MoveTo(SelectedTaskID, WeekPos);
            }
            BackAfterAction();
            return;
        }
        
        if (c == cmdDoneTask) {
            PSH.MoveTo(SelectedTaskID, ArchivePos);
            BackAfterAction();
            return;
        }
        
        if (c == cmdOK) {
            if (pos==MoneyPos)
            {
                MoneyAddEditStep=0;
                Summa=Math.abs(Summa);
                TextOperation=frmMoneyGetText.getString();
                String toSave=TextOperation+"|"+(isRashod?"-":"")+String.valueOf(Summa);
                if (MoneyFormAction==MoneyFormAction_Add)
                    PSH.Save(toSave, -1, MoneyPos);
                if (MoneyFormAction==MoneyFormAction_Edit)
                    PSH.Save(toSave, SelectedTaskID[0], MoneyPos);
                Alert myAlert = new Alert("���������", "�������� ���������!", null, AlertType.INFO);
                myAlert.setTimeout(1000);
                myAlert.setCommandListener(this);
                disp.setCurrent(myAlert);
            }
            else
            {
                if (EditFormState) {
                    PSH.Save(frmNewTask.getString(), SelectedTaskID[0], pos);
                } else {
                    PSH.Save(frmNewTask.getString(), -1, pos);
                }
                Alert myAlert = new Alert("���������", "������ ���������!", null, AlertType.INFO);
                myAlert.setTimeout(1000);
                myAlert.setCommandListener(this);
                disp.setCurrent(myAlert);
            }
            return;
        }
        if (c == cmdCancel) {
            BackAfterAction();
            return;
        }
    }

    private void ShowEditForm(String TitleText, String InitText) {
        frmNewTask = new TextBox(TitleText, InitText, 100, TextField.ANY);

        frmNewTask.addCommand(cmdOK);     
        frmNewTask.addCommand(cmdCancel);

        frmNewTask.setCommandListener(this);
        disp.setCurrent(frmNewTask);
    }

    private void ShowNewTaskForm() {
        EditFormState = false;
        ShowEditForm("����� ������", "");
    }

    private void ShowMoneyForm() {
        disp.setCurrent(frmMoney_Menu);
    }

    private void ShowListForm() {

        String strFormCapt = "";
        String str1stCapt = "", str2ndCapt = "";

        strList = null;
        intTasksID = null;

        int count = PSH.GetCount(pos);
        if (count > 0) {
            strList = new String[count];
            intTasksID = new int[count];

            PSH.LoadRecords(strList, intTasksID, pos);

            if (pos==0)
            {
                for(int i=0;i<count;i++)
                    strList[i]=strList[i].substring(0,strList[i].indexOf("|"))+" ("+strList[i].substring(strList[i].indexOf("|")+1)+" ���.)";
            }

            frmTaskList = new List("", List.MULTIPLE, strList, null);

            frmTaskList.addCommand(cmdBack);

            if (pos>0)
            {
                cmdNew = new Command("��������...", Command.SCREEN, 6);
                frmTaskList.addCommand(cmdNew);

                cmdEdit = new Command("��������...", Command.SCREEN, 5);
                frmTaskList.addCommand(cmdEdit);

                cmdDelete = new Command("�������", Command.SCREEN, 4);
                frmTaskList.addCommand(cmdDelete);

                switch (pos) {
                    case 1:
                        str1stCapt = "� ����� �� ������";
                        str2ndCapt = "� ����";
                        strFormCapt = "����� �� ����";
                        break;
                    case 2:
                        str1stCapt = "� ����� �� ����";
                        str2ndCapt = "� ����";
                        strFormCapt = "����� �� ������";
                        break;
                    case 3:
                        str1stCapt = "� ����� �� ����";
                        str2ndCapt = "� ����� �� ������";
                        strFormCapt = "����";
                        break;
                    case 4:
                        strFormCapt = "��� ����";
                        break;
                }

                if (pos != TasksPos) {
                cmdMove1st = new Command(str1stCapt, Command.SCREEN, 2);
                frmTaskList.addCommand(cmdMove1st);

                cmdMove2nd = new Command(str2ndCapt, Command.SCREEN, 3);
                frmTaskList.addCommand(cmdMove2nd);
                }

                cmdDoneTask = new Command("� ����� �������", Command.SCREEN, 1);
                frmTaskList.addCommand(cmdDoneTask);
            }
            else
            {
                strFormCapt = "��������";
                if (MoneyFormAction==MoneyFormAction_Edit){
                    cmdEdit = new Command("��������...", Command.SCREEN, 0);
                    frmTaskList.addCommand(cmdEdit);
                }
                if (MoneyFormAction==MoneyFormAction_Delete){
                    cmdDelete = new Command("�������", Command.SCREEN, 0);
                    frmTaskList.addCommand(cmdDelete);
                }
            }
        } else {
            frmTaskList = new List("", List.IMPLICIT, new String[]{"[������ ����]"}, null);
            frmTaskList.addCommand(cmdBack);

            cmdNew = new Command("��������...", Command.SCREEN, 0);
            frmTaskList.addCommand(cmdNew);
        }

        frmTaskList.setTitle(strFormCapt);
        frmTaskList.setCommandListener(this);

        disp.setCurrent(frmTaskList);
    }

    public void startApp() {
        disp = Display.getDisplay(this);
        disp.setCurrent(frmMain);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        PSH.CloseSession();
        notifyDestroyed();
    }
}

