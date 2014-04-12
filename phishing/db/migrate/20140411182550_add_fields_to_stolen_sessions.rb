class AddFieldsToStolenSessions < ActiveRecord::Migration
  def change
    add_column :stolen_sessions, :session_id, :string
    add_column :stolen_sessions, :ip_address, :string
  end
end
