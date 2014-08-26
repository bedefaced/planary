package global;

import javax.microedition.rms.*;
import java.util.Date;

public class RecordStoreHelper implements RecordFilter{

    private static final String DBNAME = "db_notes";
    private RecordStore RS,RS2;
    private int PosFilter;
    
    public RecordStoreHelper(){
        OpenSession();
    }

    public void CloseSession(){
        try{
            RS.closeRecordStore();
        }
        catch(Exception e){System.out.print("Error in CloseSession(): "+e.getMessage());}
    }

    public void Update()
    {
        boolean found;
        int count=this.GetCount(6);
        if (count==0) found=false;
        else found=true;

        if (!found)
        {
            int len[]=new int[4];
            for(int i=0;i<4;i++)
                len[i]=this.GetCount(i);

            int OutID0[]=new int[len[0]];
            int OutID1[]=new int[len[1]];
            int OutID2[]=new int[len[2]];
            int OutID3[]=new int[len[3]];

            this.LoadRecordsID(OutID0, 0);
            this.LoadRecordsID(OutID1, 1);
            this.LoadRecordsID(OutID2, 2);
            this.LoadRecordsID(OutID3, 3);

            this.MoveTo(OutID0, 1);
            this.MoveTo(OutID1, 2);
            this.MoveTo(OutID2, 3);
            this.MoveTo(OutID3, 5);

            this.Save("newVersion=true", -1, 6);
        }
    }

    public void OpenSession(){
        try{
            RS = RecordStore.openRecordStore(DBNAME, true);
        }
        catch(Exception e){System.out.print("Error in OpenSession(): "+e.getMessage());}
    }

    public void Save(String txtTask, int ID, int pos){
        try{
            Date dt=new Date();
            String OutString=String.valueOf(dt.getTime())+"|"+String.valueOf(pos)+"|"+txtTask;
            if (ID>=0)
                RS.setRecord(ID, OutString.getBytes("UTF-8"), 0, OutString.getBytes("UTF-8").length);
            else
                RS.addRecord(OutString.getBytes("UTF-8"), 0, OutString.getBytes("UTF-8").length);
           }
           catch(Exception e){System.out.print("Error in Save(): "+e.getMessage());}
    }

    public void Delete(int ID[]){
        try{
            for(int i=0;i<ID.length;i++)
                RS.deleteRecord(ID[i]);
           }
           catch(Exception e){System.out.print("Error in Delete(): "+e.getMessage());}
    }

    public void MoveTo(int ID[], int Distination){
        try{
            for(int i=0;i<ID.length;i++)
            {
                byte bytRec[]=RS.getRecord(ID[i]);
                String strRec=new String(bytRec,"UTF-8");
                Date dt=new Date();
                String strDate=String.valueOf(dt.getTime())+"|";
                int posit=strRec.indexOf("|")+1;
                strRec=strDate+String.valueOf(Distination)+strRec.substring(strRec.indexOf("|",posit));
                RS.setRecord(ID[i], strRec.getBytes("UTF-8"), 0, strRec.getBytes("UTF-8").length);
            }
           }
           catch(Exception e){System.out.print("Error in MoveTo(): "+e.getMessage());}
    }

    public int GetCount(int pos){
        int outs=0;
        try{
        PosFilter=pos;
        RecordEnumeration re=RS.enumerateRecords(this, null, true);
        if (re!=null)
            outs=re.numRecords();
        }
        catch(Exception e){System.out.print("Error in GetCount(): "+e.getMessage());}
        return outs;
    }

    public boolean matches(byte[] candidate){
        try{
        String strTemp=new String(candidate);
        int iod=strTemp.indexOf("|");
        if (strTemp.substring(iod+1,strTemp.indexOf("|",iod+1)).equals(String.valueOf(PosFilter)))
            return true;
        }
        catch(Exception e){System.out.print("Error in matches(): "+e.getMessage());}
        return false;
    }

    public void LoadRecordsID(int OutIntTasksID[], int pos){
        try{
            PosFilter=pos;
            RecordEnumeration re=RS.enumerateRecords(this, null, true);
            int i=0,intID;

            while (re.hasNextElement())
            {
                intID=re.nextRecordId();
                OutIntTasksID[i]=intID;
                i++;
            }

           }
           catch(Exception e){System.out.print("Error in LoadRecords(): "+e.getMessage());}
    }

    public String LoadRecordByID(int ID){
        String OutString="";
        try{
                byte bytRec[]=new byte[RS.getRecordSize(ID)];
                bytRec=RS.getRecord(ID);
                OutString=new String(bytRec,"UTF-8");
                int iod=OutString.indexOf("|"); //only text, no date & no position
                OutString=OutString.substring(OutString.indexOf("|",iod+1)+1);
           }
           catch(Exception e){System.out.print("Error in LoadRecords(): "+e.getMessage());}
        return OutString;
    }

    public void LoadRecords(String OutString[], int OutIntTasksID[], int pos){
        try{
            PosFilter=pos;
            RecordEnumeration re=RS.enumerateRecords(this, null, true);
            int i=0,intID;

            while (re.hasNextElement())
            {
                intID=re.nextRecordId();
                byte bytRec[]=new byte[RS.getRecordSize(intID)];
                bytRec=RS.getRecord(intID);
                OutString[i]=new String(bytRec,"UTF-8");
                int iod=OutString[i].indexOf("|"); //only text, no date & no position
                OutString[i]=OutString[i].substring(OutString[i].indexOf("|",iod+1)+1);
                OutIntTasksID[i]=intID;
                i++;
            }

           }
           catch(Exception e){System.out.print("Error in LoadRecords(): "+e.getMessage());}
    }

    public void LoadArchiveRecords(String OutString[], Long Dates[]){
        try{
            PosFilter=5;
            RecordEnumeration re=RS.enumerateRecords(this, null, true);
            int i=0,intID;

            while (re.hasNextElement())
            {
                intID=re.nextRecordId();
                byte bytRec[]=new byte[RS.getRecordSize(intID)];
                bytRec=RS.getRecord(intID);
                OutString[i]=new String(bytRec,"UTF-8");
                int iod=OutString[i].indexOf("|"); //only text, no date & no position
                Dates[i]=new Long(Long.parseLong(OutString[i].substring(0,OutString[i].indexOf("|"))));
                OutString[i]=OutString[i].substring(OutString[i].indexOf("|",iod+1)+1);
                i++;
            }

            //sort by Date
            for(i=0;i<Dates.length-1;i++)
                for(int j=i+1;j<Dates.length;j++)
                {
                if (Dates[i].longValue()<Dates[j].longValue()){
                        //simple sort: swap elements

                        long lnTemp=Dates[i].longValue();
                        Dates[i]=new Long(Dates[j].longValue());
                        Dates[j]=new Long(lnTemp);

                        String strTemp=String.valueOf(OutString[i]);
                        OutString[i]=new String(OutString[j]);
                        OutString[j]=new String(strTemp);
                    }
                }
           }
           catch(Exception e){System.out.print("Error in LoadArchiveRecords(): "+e.getMessage());}
    }

    public void ClearArchiveRecords(){
        try{
            PosFilter=5;
            RecordEnumeration re=RS.enumerateRecords(this, null, true);
            int i=0,intID;

            while (re.hasNextElement())
            {
                intID=re.nextRecordId();
                RS.deleteRecord(intID);
            }

           }
           catch(Exception e){System.out.print("Error in LoadArchiveRecords(): "+e.getMessage());}
    }
}