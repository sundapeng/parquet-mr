package redelm.data.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import redelm.Log;
import redelm.data.Group;
import redelm.schema.GroupType;
import redelm.schema.Type;

public class SimpleGroup extends Group {
  private static final Logger logger = Logger.getLogger(SimpleGroup.class.getName());

  private static final boolean DEBUG = Log.DEBUG;

  private final GroupType schema;
  private final List<Object>[] data;

  @SuppressWarnings("unchecked")
  public SimpleGroup(GroupType schema) {
    this.schema = schema;
    this.data = new List[schema.getFields().size()];
    for (int i = 0; i < schema.getFieldCount(); i++) {
       this.data[i] = new ArrayList<Object>();
    }
  }

  @Override
  public String toString() {
    return toString("");
  }

  public String toString(String indent) {
    String result = "";
    int i = 0;
    for (Type field : schema.getFields()) {
      String name = field.getName();
      List<Object> values = data[i];
      ++i;
      if (values != null) {
        if (values.size() > 0) {
          for (Object value : values) {
            result += indent + name;
            if (value == null) {
              result += ": NULL\n";
            } else if (value instanceof Group) {
              result += "\n" + ((SimpleGroup)value).toString(indent+"  ");
            } else {
              result += ": " + value.toString() + "\n";
            }
          }
        }
      }
    }
    return result;
  }

  @Override
  public Group addGroup(int fieldIndex) {
    SimpleGroup g = new SimpleGroup(schema.getType(fieldIndex).asGroupType());
    data[fieldIndex].add(g);
    return g;
  }

  @Override
  public Group getGroup(int fieldIndex, int index) {
    return (Group)getValue(fieldIndex, index);
  }

  private Object getValue(int fieldIndex, int index) {
    List<Object> list;
    try {
      list = data[fieldIndex];
    } catch (IndexOutOfBoundsException e) {
      throw new RuntimeException("not found " + fieldIndex + "(" + schema.getFieldName(fieldIndex) + ") in group:\n" + this);
    }
    try {
      return list.get(index);
    } catch (IndexOutOfBoundsException e) {
      throw new RuntimeException("not found " + fieldIndex + "(" + schema.getFieldName(fieldIndex) + ") element number " + index + " in group:\n" + this);
    }
  }

  private void add(int fieldIndex, Primitive value) {
    Type type = schema.getType(fieldIndex);
    List<Object> list = data[fieldIndex];
    if (type.getRepetition() != Type.Repetition.REPEATED
        && !list.isEmpty()) {
      throw new IllegalStateException("field "+fieldIndex+" (" + type.getName() + ") can not have more than one value: " + list);
    }
    list.add(value);
  }

  @Override
  public int getFieldRepetitionCount(int fieldIndex) {
    List<Object> list = data[fieldIndex];
    return list == null ? 0 : list.size();
  }

  @Override
  public String getString(int fieldIndex, int index) {
    return ((StringValue)getValue(fieldIndex, index)).getString();
  }

  @Override
  public int getInt(int fieldIndex, int index) {
    return ((IntValue)getValue(fieldIndex, index)).getInt();
  }

  @Override
  public boolean getBool(int fieldIndex, int index) {
    return ((BoolValue)getValue(fieldIndex, index)).getBool();
  }

  @Override
  public byte[] getBinary(int fieldIndex, int index) {
    return ((BinaryValue)getValue(fieldIndex, index)).getBinary();
  }

  @Override
  public void add(int fieldIndex, int value) {
    add(fieldIndex, new IntValue(value));
  }

  @Override
  public void add(int fieldIndex, String value) {
    add(fieldIndex, new StringValue(value));
  }

  @Override
  public void add(int fieldIndex, boolean value) {
    add(fieldIndex, new BoolValue(value));
  }

  @Override
  public void add(int fieldIndex, byte[] value) {
    add(fieldIndex, new BinaryValue(value));
  }

  @Override
  public GroupType getType() {
    return schema;
  }

}
