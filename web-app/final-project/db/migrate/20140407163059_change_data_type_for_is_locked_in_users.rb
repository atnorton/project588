class ChangeDataTypeForIsLockedInUsers < ActiveRecord::Migration
  def change
    change_table :users do |t|
      t.change :is_locked, :datetime
    end
  end
end
