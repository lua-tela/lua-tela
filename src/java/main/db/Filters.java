package main.db;

import com.hk.lua.LuaObject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import main.db.Query.Filter;

public class Filters
{    
    public static class CompareFilter extends Filter
    {
        private final Model model;
        private final DataField field;
        private final LuaObject value;
        private final int state;
        
        public CompareFilter(Model model, DataField field, LuaObject value, int state)
        {
            this.model = model;
            this.field = field;
            this.value = field.clean(value);
            this.state = check(state);
        }
        
        @Override
        public void append(Query query, StringBuilder sb)
        {
            field.appendName(model, sb, true);
            
            switch(state)
            {
                case EQUALS:
                    sb.append(" = ?");
                    break;
                case LESS_THAN:
                    sb.append(" < ?");
                    break;
                case GRTR_THAN:
                    sb.append(" > ?");
                    break;
                case LSEQ_THAN:
                    sb.append(" <= ?");
                    break;
                case GREQ_THAN:
                    sb.append(" >= ?");
                    break;
            }
        }

        @Override
        public int apply(Query query, PreparedStatement stmt, int index)
        {
            try
            {
                field.toJavaObject(stmt, index, value);
            }
            catch (SQLException ex)
            {
                throw new RuntimeException(ex);
            }
            return 1;
        }
        
        private static int check(int state)
        {
            switch(state)
            {
                case EQUALS:
                case LESS_THAN:
                case GRTR_THAN:
                case LSEQ_THAN:
                case GREQ_THAN:
                    return state;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }
    
    public static final int EQUALS = 0;
    public static final int LESS_THAN = 1;
    public static final int GRTR_THAN = 2;
    public static final int LSEQ_THAN = 3;
    public static final int GREQ_THAN = 4;
    
    private Filters()
    {}
}
