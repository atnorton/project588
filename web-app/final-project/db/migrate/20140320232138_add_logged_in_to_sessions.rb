class AddLoggedInToSessions < ActiveRecord::Migration
  def change
    add_column :sessions, :logged_in, :boolean
  end
end
