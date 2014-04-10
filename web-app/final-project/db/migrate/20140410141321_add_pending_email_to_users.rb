class AddPendingEmailToUsers < ActiveRecord::Migration
  def change
    add_column :users, :pending_email, :string
  end
end
