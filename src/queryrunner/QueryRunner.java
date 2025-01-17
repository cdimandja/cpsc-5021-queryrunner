/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package queryrunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This is where the code will actually run, sql. It will connect to SQL and manage the connection.
 * <p>
 * QueryRunner takes a list of Queries that are initialized in it's constructor
 * and provides functions that will call the various functions in the QueryJDBC class
 * which will enable MYSQL queries to be executed. It also has functions to provide the
 * returned data from the Queries. Currently the eventHandlers in QueryFrame call these
 * functions in order to run the Queries.
 */
public class QueryRunner
{
    public QueryRunner() throws IOException
    {
        this.m_jdbcData = new QueryJDBC();
        m_updateAmount = 0;
        m_error = "";

        this.m_projectTeamApplication = "ReelHype";
        m_queryParamMap = readQueryParams();
        m_queryArray = readQueries();
        m_queryArrayDescription = readQueriesDescriptions();
    }

    public ArrayList<QueryData> readQueries() throws IOException
    {
        ArrayList<QueryData> queries = new ArrayList<>();

        Scanner file = new Scanner(new File(queryFilePath));
        int lineCount = 1;
        while (file.hasNext())
        {
            String query = file.nextLine();
            int params = query.split("\\?", -1).length - 1;
            String[] paramStrings = new String[params];
            boolean[] likeParams = new boolean[params];
            for (int i = 0; i < paramStrings.length; i++)
            {
                paramStrings[i] = m_queryParamMap.getName(lineCount, i);
                likeParams[i] = m_queryParamMap.isLike(lineCount, i);
            }
            boolean isAction = query.contains("insert") || query.contains("update");
            queries.add(new QueryData(query, paramStrings, likeParams, isAction, params > 0));
            lineCount++;
        }
        file.close();
        return queries;
    }

    public ArrayList<String> readQueriesDescriptions() throws IOException
    {
        ArrayList<String> queries = new ArrayList<>();

        Scanner file = new Scanner(new File(queryDescFilePath));
        while (file.hasNext())
        {
            queries.add(file.nextLine());
        }
        file.close();
        return queries;
    }

    public QueryParams readQueryParams() throws IOException
    {

        QueryParams output = new QueryParams();

        Scanner file = new Scanner(new File(queryParamFilePath));
        while (file.hasNext())
        {
            String[] csv = file.nextLine().split(",");
            int queryNum = Integer.parseInt(csv[0]);
            int paramNum = Integer.parseInt(csv[1]);
            String paramName = csv[2];
            boolean isLike = Boolean.parseBoolean(csv[3]);

            output.addParam(queryNum, paramNum, paramName, isLike);
        }

        file.close();

        return output;
    }

    public String getQueryDescription(int index)
    {
        return m_queryArrayDescription.get(index);
    }

    public int GetTotalQueries()
    {
        return m_queryArray.size();
    }

    public int GetParameterAmtForQuery(int queryChoice)
    {
        QueryData e = m_queryArray.get(queryChoice);
        return e.GetParmAmount();
    }

    public String GetParamText(int queryChoice, int parmnum)
    {
        QueryData e = m_queryArray.get(queryChoice);
        return e.GetParamText(parmnum);
    }

    public String GetQueryText(int queryChoice)
    {
        QueryData e = m_queryArray.get(queryChoice);
        return e.GetQueryString();
    }

    /**
     * Function will return how many rows were updated as a result
     * of the update query
     *
     * @return Returns how many rows were updated
     */

    public int GetUpdateAmount()
    {
        return m_updateAmount;
    }

    /**
     * Function will return ALL of the Column Headers from the query
     *
     * @return Returns array of column headers
     */
    public String[] GetQueryHeaders()
    {
        return m_jdbcData.GetHeaders();
    }

    /**
     * After the query has been run, all of the data has been captured into
     * a multi-dimensional string array which contains all the row's. For each
     * row it also has all the column data. It is in string format
     *
     * @return multi-dimensional array of String data based on the resultset
     * from the query
     */
    public String[][] GetQueryData()
    {
        return m_jdbcData.GetData();
    }

    public String GetProjectTeamApplication()
    {
        return m_projectTeamApplication;
    }

    public boolean isActionQuery(int queryChoice)
    {
        QueryData e = m_queryArray.get(queryChoice);
        return e.IsQueryAction();
    }

    public boolean isParameterQuery(int queryChoice)
    {
        QueryData e = m_queryArray.get(queryChoice);
        return e.IsQueryParm();
    }


    public boolean ExecuteQuery(int queryChoice, String[] parms)
    {
        boolean bOK = true;
        QueryData e = m_queryArray.get(queryChoice);
        bOK = m_jdbcData.ExecuteQuery(e.GetQueryString(), parms, e.GetAllLikeParams());
        return bOK;
    }

    public boolean ExecuteUpdate(int queryChoice, String[] parms)
    {
        boolean bOK = true;
        QueryData e = m_queryArray.get(queryChoice);
        bOK = m_jdbcData.ExecuteUpdate(e.GetQueryString(), parms);
        m_updateAmount = m_jdbcData.GetUpdateCount();
        return bOK;
    }

    public boolean Connect(String szHost, String szUser, String szPass, String szDatabase)
    {
        boolean bConnect = m_jdbcData.ConnectToDatabase(szHost, szUser, szPass, szDatabase);
        if (bConnect == false)
            m_error = m_jdbcData.GetError();
        return bConnect;
    }

    public boolean Disconnect()
    {
        // Disconnect the JDBCData Object
        boolean bConnect = m_jdbcData.CloseDatabase();
        if (bConnect == false)
            m_error = m_jdbcData.GetError();
        return true;
    }

    public String GetError()
    {
        return m_error;
    }

    public void ClearError()
    {
        m_error = null;
    }

    private QueryJDBC m_jdbcData;
    private String m_error;
    private String m_projectTeamApplication;
    private ArrayList<QueryData> m_queryArray;
    private ArrayList<String> m_queryArrayDescription;
    private QueryParams m_queryParamMap;
    private int m_updateAmount;

    ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    final private String queryFilePath = classloader
            .getResource("Queries.txt").getFile();
    final private String queryDescFilePath = classloader
            .getResource("QueriesDescription.txt").getFile();
    final private String queryParamFilePath = classloader
            .getResource("QueryParameters.txt").getFile();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException
    {
        final QueryRunner queryrunner = new QueryRunner();

        if (args.length == 0)
        {
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    new QueryFrame(queryrunner).setVisible(true);
                }
            });
        } else
        {
            if (args[0].equals("-console"))
            {
                QueryConsole.main(args, queryrunner);
                queryrunner.Disconnect();
            }
        }

    }
}
