class AddSessionIdToSessions < ActiveRecord::Migration
  def change
    add_column :sessions, :session_id, :string
    add_index :sessions, :session_id
  end
end
