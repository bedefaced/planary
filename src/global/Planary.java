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

    String[] strLargeMonths = {"января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
    };
    String[] strMonths = {"янв", "фев", "март", "апр", "мая", "июн",
        "июл", "авг", "сент", "окт", "нояб", "дек"
    };
    String[] strLargeDayOfWeek = {"воскресенье", "понедельник", "вторник", "среда",
        "четверг", "пятница", "суббота"
    };
    String[] strDayOfWeek = {"вскр", "пнд", "втрн", "срд", "чтв", "птн", "сбт"};

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
                strMonths[m] + " " + Integer.toString(y) + " г. (" + strDayOfWeek[w] + ")");
        String[] strMenuList = {"Мой кошелёк", "Сегодня", "Неделя", "Хаос", "Мои цели", "Архив успехов"};
        String[] MoneyMenuList = {"Добавить", "Изменить", "Удалить", "Баланс"};

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

        cmdSelect = new Command("Выбор", Command.ITEM, 0);
        frmMain.addCommand(cmdSelect);

        cmdExit = new Command("Выход", Command.EXIT, 0);
        frmMain.addCommand(cmdExit);

        cmdBack = new Command("Назад", Command.BACK,1); //общая для всех команда Back, возвращает в главное меню
        cmdNext=new Command("Далее",Command.ITEM,0);
        cmdOK = new Command("OK", Command.OK, 1);
        cmdCancel = new Command("Назад", Command.CANCEL, 1);

        String dirList[]={"расход","доход"};
        frmMoneyDirection=new List("Направление",List.EXCLUSIVE,dirList,null);
        frmMoneyDirection.addCommand(cmdNext);
        frmMoneyDirection.addCommand(cmdBack);
        frmMoneyDirection.setCommandListener(this);

        frmMoney_Menu = new List("",List.IMPLICIT,MoneyMenuList,null);
        frmMoney_Menu.setTitle("Операции");
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
        Form frmBalance=new Form("Баланс");
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

        StringItem si1 = new StringItem("","Баланс: ", StringItem.PLAIN);
        si1.setFont(bold_styled);
        frmBalance.append(si1);
        frmBalance.append(String.valueOf(balance));
        frmBalance.append("руб.\n");

        StringItem si2 = new StringItem("", "Доходы: ", StringItem.PLAIN);
        si2.setFont(bold_styled);
        frmBalance.append(si2);
        frmBalance.append(String.valueOf(dohod));
        frmBalance.append("руб.\n");

        StringItem si3 = new StringItem("", "Расходы: ", StringItem.PLAIN);
        si3.setFont(bold_styled);
        frmBalance.append(si3);
        frmBalance.append(String.valueOf(rashod));
        frmBalance.append("руб.\n");

        frmBalance.addCommand(cmdBack);
        frmBalance.setCommandListener(this);
        disp.setCurrent(frmBalance);
    }

    private void ShowArchive() {
        frmArchive = new Form("Архив успехов");

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

                String strDate = new String(String.valueOf(d1) + " " + strMonths[m1] + " " + String.valueOf(y1) + " г. :");
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

            cmdClear = new Command("Очистить", Command.SCREEN, 0);
            frmArchive.addCommand(cmdClear);
        } else {
            frmArchive.append("[Записей пока нет]");
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
                frmMoneyGetText=new TextBox("Сумма", (MoneyFormAction==MoneyFormAction_Add)?"":String.valueOf(Summa), 10, TextField.DECIMAL);
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
                frmMoneyGetText=new TextBox("Примечание", TextOperation, 100, TextField.ANY);
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
                ShowEditForm("Изменение задачи", frmTaskList.getString(GetFirstTrueIndex(SelectedArray)));
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
                Alert myAlert = new Alert("Выполнено", "Операция сохранена!", null, AlertType.INFO);
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
                Alert myAlert = new Alert("Выполнено", "Задача сохранена!", null, AlertType.INFO);
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
        ShowEditForm("Новая задача", "");
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
                    strList[i]=strList[i].substring(0,strList[i].indexOf("|"))+" ("+strList[i].substring(strList[i].indexOf("|")+1)+" руб.)";
            }

            frmTaskList = new List("", List.MULTIPLE, strList, null);

            frmTaskList.addCommand(cmdBack);

            if (pos>0)
            {
                cmdNew = new Command("Добавить...", Command.SCREEN, 6);
                frmTaskList.addCommand(cmdNew);

                cmdEdit = new Command("Изменить...", Command.SCREEN, 5);
                frmTaskList.addCommand(cmdEdit);

                cmdDelete = new Command("Удалить", Command.SCREEN, 4);
                frmTaskList.addCommand(cmdDelete);

                switch (pos) {
                    case 1:
                        str1stCapt = "В планы на неделю";
                        str2ndCapt = "В Хаос";
                        strFormCapt = "Планы на день";
                        break;
                    case 2:
                        str1stCapt = "В планы на день";
                        str2ndCapt = "В Хаос";
                        strFormCapt = "Планы на неделю";
                        break;
                    case 3:
                        str1stCapt = "В планы на день";
                        str2ndCapt = "В планы на неделю";
                        strFormCapt = "Хаос";
                        break;
                    case 4:
                        strFormCapt = "Мои цели";
                        break;
                }

                if (pos != TasksPos) {
                cmdMove1st = new Command(str1stCapt, Command.SCREEN, 2);
                frmTaskList.addCommand(cmdMove1st);

                cmdMove2nd = new Command(str2ndCapt, Command.SCREEN, 3);
                frmTaskList.addCommand(cmdMove2nd);
                }

                cmdDoneTask = new Command("В архив успехов", Command.SCREEN, 1);
                frmTaskList.addCommand(cmdDoneTask);
            }
            else
            {
                strFormCapt = "Операции";
                if (MoneyFormAction==MoneyFormAction_Edit){
                    cmdEdit = new Command("Изменить...", Command.SCREEN, 0);
                    frmTaskList.addCommand(cmdEdit);
                }
                if (MoneyFormAction==MoneyFormAction_Delete){
                    cmdDelete = new Command("Удалить", Command.SCREEN, 0);
                    frmTaskList.addCommand(cmdDelete);
                }
            }
        } else {
            frmTaskList = new List("", List.IMPLICIT, new String[]{"[Список пуст]"}, null);
            frmTaskList.addCommand(cmdBack);

            cmdNew = new Command("Добавить...", Command.SCREEN, 0);
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

